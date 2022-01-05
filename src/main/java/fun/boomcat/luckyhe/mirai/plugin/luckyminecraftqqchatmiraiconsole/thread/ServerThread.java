package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.thread;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.config.ConfigOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.SessionDataNotExistException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.pojo.MinecraftData;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.pojo.Packet;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.util.ConnectionPacketReceiveUtil;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.util.ConnectionPacketSendUtil;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.Session;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.SessionUtil;
import net.mamoe.mirai.utils.MiraiLogger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {
    private MiraiLogger logger;

    public ServerThread(MiraiLogger logger) {
        this.logger = logger;
    }

    @Override
    public void run() {
        ServerSocket serverSocket;
        int port;
        int heartbeat;
        try {
            port = ConfigOperation.getPort();
            heartbeat = ConfigOperation.getHeartbeat();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取配置文件失败，请尝试重启程序");
            return;
        }

        try {
            serverSocket = new ServerSocket(port);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Server Socket开启失败，请尝试重启程序");
            return;
        }

        logger.info("Server Socket开启成功，监听端口为" + port);

        while (true) {
            Socket socket;
            try {
                socket = serverSocket.accept();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("等待连接期间出现错误");
                continue;
            }

            logger.info("接收到来自" + socket.getRemoteSocketAddress() + "的连接");

            BufferedInputStream inputStream;
            BufferedOutputStream outputStream;
            try {
                inputStream = new BufferedInputStream(socket.getInputStream());
                outputStream = new BufferedOutputStream(socket.getOutputStream());
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("获取输入输出流期间出现错误");
                continue;
            }

//            开始接收来自客户端的初始化信息
            Packet packet;
            try {
                packet = ConnectionPacketReceiveUtil.getPacket(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("获取连接数据包时出现错误");
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                continue;
            }

//            不按照约定发送数据包
            if (packet.getId().getValue() != 0x00) {
                logger.error("未按照约定发送数据包，拒绝连接");
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                continue;
            }

//            发来的数据包转为对象
            MinecraftData minecraftData;
            try {
                minecraftData = ConnectionPacketReceiveUtil.getMinecraftData(packet);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("读取包时出现错误，断开连接");
                continue;
            }

//            获取对应session
            Session session;
            try {
                session = SessionUtil.getSession(minecraftData.getSessionId().getValue());
            } catch (SessionDataNotExistException e) {
//                发送错误信息数据包
                try {
                    outputStream.write(ConnectionPacketSendUtil.getErrorPacket("没有会话号为" + minecraftData.getSessionId().getValue() + "的会话").getBytes());
                    outputStream.flush();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    logger.error("在发送错误信息数据包时发生错误");

                    try {
                        socket.close();
                    } catch (IOException exc) {
                        exc.printStackTrace();
                    }

                    continue;
                }

                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                continue;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("出现其它错误");
                continue;
            }

            try {
                outputStream.write(ConnectionPacketSendUtil.getCorrectResponsePacket(
                        session.getName(),
                        socket.getRemoteSocketAddress().toString()
                ).getBytes());
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("发送响应信息数据包时发生错误");

                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                continue;
            }

//            存入到Session中

        }
    }
}
