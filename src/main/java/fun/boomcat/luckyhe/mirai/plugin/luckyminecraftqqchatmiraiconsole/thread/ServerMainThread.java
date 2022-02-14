package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.thread;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.config.ConfigOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.SessionDataNotExistException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.pojo.Packet;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.util.ConnectionPacketReceiveUtil;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.util.ConnectionPacketSendUtil;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.Session;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.SessionUtil;
import net.mamoe.mirai.utils.MiraiLogger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;

public class ServerMainThread extends Thread {
    private final MiraiLogger logger;
    private ServerSocket serverSocket;
    private boolean isRunning = true;

    public ServerMainThread(MiraiLogger logger) {
        this.logger = logger;
    }

    public void close() {
        isRunning = false;

        while (!serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        int port;
        try {
            port = ConfigOperation.getPort();
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
        for (int i = 10; i > 0; i--) {
            logger.info(i + "秒后开始监听请求");
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        while (isRunning) {
            logger.info("开始监听来自" + port + "端口的请求");
            Socket socket;
            try {
                socket = serverSocket.accept();
            } catch (Exception e) {
//                若serverSocket被关闭，异常会被此处捕捉
                if (isRunning) {
//                    只有正常运行出现错误才输出如下信息
                    e.printStackTrace();
                    logger.error("等待连接期间出现错误");
                }
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
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
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
            MinecraftConnectionThread minecraftConnectionThread;
            try {
//                传入socket（此时不代表线程会启动）
                minecraftConnectionThread = ConnectionPacketReceiveUtil.getMinecraftConnectionThread(packet, socket);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("读取包时出现错误，断开连接");
                continue;
            }

            logger.info("请求会话号" + minecraftConnectionThread.getSessionId().getValue() + "，服务器名称：" + minecraftConnectionThread.getServerName().getContent());

//            获取对应session
            Session session;
            try {
                session = SessionUtil.getSession(minecraftConnectionThread.getSessionId().getValue());
            } catch (SessionDataNotExistException e) {
//                发送错误信息数据包
                try {
                    logger.error("没有会话号为" + minecraftConnectionThread.getSessionId().getValue() + "的会话");
                    outputStream.write(ConnectionPacketSendUtil.getErrorPacket("没有会话号为" + minecraftConnectionThread.getSessionId().getValue() + "的会话").getBytes());
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

            String serverName = minecraftConnectionThread.getServerName().getContent();
            if (serverName.contains(" ") || serverName.contains("\n") || serverName.contains("\r")) {
                try {
                    logger.error("服务器名称不要携带分隔符（如空格，换行符等）");
                    outputStream.write(ConnectionPacketSendUtil.getErrorPacket("服务器名称不要携带分隔符（如空格，换行符等）").getBytes());
                    outputStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("在发送错误信息数据包时发生错误");

                    try {
                        socket.close();
                    } catch (IOException exc) {
                        exc.printStackTrace();
                    }
                }

                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                continue;
            }

//            判断该会话中是否已经存在同名MC服务器，如果存在，则拒绝连接
//            该功能存在问题，可能会流到下一个版本解决
//            if (session.isMinecraftServerNameExist(minecraftConnectionThread.getServerName().getContent())) {
//                try {
//                    logger.error("携带的服务器名称" + minecraftConnectionThread.getServerName().getContent() + "已经接入了会话");
//                    outputStream.write(ConnectionPacketSendUtil.getErrorPacket("携带的服务器名称" + minecraftConnectionThread.getServerName().getContent() + "已经接入了会话").getBytes());
//                    outputStream.flush();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    logger.error("在发送错误信息数据包时发生错误");
//
//                    try {
//                        socket.close();
//                    } catch (IOException exc) {
//                        exc.printStackTrace();
//                    }
//                }
//
//                try {
//                    socket.close();
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
//
//                continue;
//            }

            try {
                outputStream.write(ConnectionPacketSendUtil.getCorrectResponsePacket(
                        session.getName(),
                        socket.getRemoteSocketAddress().toString(),
                        ConfigOperation.getHeartbeat()
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

//            存入到MinecraftData中
            logger.info("来自" + socket.getRemoteSocketAddress() + "的连接数据包验证完成，数据正确，进入通信阶段");
            minecraftConnectionThread.setDaemon(false);
            minecraftConnectionThread.start();

//            向群内公告此连接
            session.sendMessageToAllGroups("有Minecraft服务端接入会话！\n会话名：" + session.getName() +"\n服务端名称：" + minecraftConnectionThread.getServerName().getContent() + "\n地址：" + minecraftConnectionThread.getServerAddress() + "\n时间：" + new Date());

//            添加到同一会话的线程列表和传入会话对象
            session.addMinecraftThread(minecraftConnectionThread);
            minecraftConnectionThread.setSession(session);
        }

        logger.info("监听线程结束");
    }
}
