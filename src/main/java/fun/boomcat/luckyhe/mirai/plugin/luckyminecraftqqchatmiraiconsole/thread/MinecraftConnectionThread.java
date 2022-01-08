package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.thread;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.config.ConfigOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.datatype.VarInt;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.datatype.VarIntString;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.datatype.VarLong;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.pojo.Packet;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.util.ConnectionPacketReceiveUtil;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.Session;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.MinecraftFormatPlaceholder;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.MiraiLoggerUtil;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.ReplacePlaceholderUtil;
import net.mamoe.mirai.utils.MiraiLogger;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class MinecraftConnectionThread extends Thread {
    private VarLong sessionId;
    private Session session;
    private VarIntString serverName;
    private VarIntString joinFormatString;
    private VarIntString quitFormatString;
    private VarIntString msgFormatString;
    private VarIntString deathFormatString;
    private VarIntString kickFormatString;

    private String serverAddress;

    private boolean isConnect = true;

    private final MiraiLogger logger = MiraiLoggerUtil.getLogger();

    private final Queue<Packet> sendQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Packet> receiveQueue = new ConcurrentLinkedQueue<>();

    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    private CountDownLatch cdl = new CountDownLatch(4);

//    发送ping包后修改该值，若响应正确则重新置为0
    private Long pingNumber = 0L;

    public String getStringWithPrefix(String threadName, String info) {
        return "[" + serverName.getContent() + serverAddress + "][" + threadName + "] " + info;
    }

    public void logInfo(String threadName, String info) {
        logger.info(getStringWithPrefix(threadName, info));
    }

    public void logError(String threadName, String error) {
        logger.error(getStringWithPrefix(threadName, error));
    }

    public void sendClosePacket(String info) {
        VarInt packetId = new VarInt(0xF0);
        VarIntString string = new VarIntString(info);
        addSendQueue(new Packet(new VarInt(packetId.getBytesLength() + string.getBytesLength()), packetId, string.getBytes()));
    }

    @Override
    public void run() {
        //                    如果发送数据包为关闭包
        Thread sendThread = new Thread(() -> {
            String threadName = "发送";
            logInfo(threadName, "线程启动");
            while (isConnect) {
                try {
                    Packet packet = sendQueue.poll();
                    if (packet != null) {
                        outputStream.write(packet.getBytes());
                        outputStream.flush();

                        //                    如果发送数据包为关闭包
                        if (packet.getId().getValue() == 0xF0) {
                            logInfo(threadName, "发送关闭包，内容：" + new VarIntString(packet.getData()).getContent());
                            isConnect = false;
                            socket.close();
                        }
                    }
                } catch (Exception e) {
                    isConnect = false;
                    e.printStackTrace();
                    logError(threadName, "线程出现异常，开始关闭Socket");

                    try {
                        socket.close();
                        logInfo(threadName, "Socket关闭成功");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        logError(threadName, "Socket关闭失败");
                    }
                }
            }
            cdl.countDown();
            logInfo(threadName, "结束工作");
        });

        Thread receiveThread = new Thread(() -> {
            String threadName = "接收";
            logInfo(threadName, "线程启动");
            while (isConnect) {
                try {
                    if (inputStream.available() > 0) {
                        Packet packet = ConnectionPacketReceiveUtil.getPacket(inputStream);
                        receiveQueue.add(packet);
                    }

                } catch (Exception e) {
                    isConnect = false;
                    e.printStackTrace();
                    logError(threadName, "线程出现异常，开始关闭Socket");

                    try {
                        socket.close();
                        logInfo(threadName, "Socket关闭成功");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        logError(threadName, "Socket关闭失败");
                    }
                }
            }

            cdl.countDown();
            logInfo(threadName, "结束工作");
        });

        //                    对方未回应的情况（如果对方回应了，pingNumber应该为0）
        //                生成一个不为0的ping数
        Thread heartbreakThread = new Thread(() -> {
            String threadName = "心跳";
            logInfo(threadName, "线程启动");
            Random random = new Random();
            while (isConnect) {
                try {
                    Thread.sleep(1000 * ConfigOperation.getHeartbeat());
                } catch (Exception e) {
                    isConnect = false;
                    e.printStackTrace();
                    logError(threadName, "线程异常停止，开始关闭Socket");

                    try {
                        socket.close();
                        logInfo(threadName, "Socket关闭成功");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        logError(threadName, "Socket关闭失败");
                    }
                }

                if (pingNumber != 0) {
//                    对方未回应的情况（如果对方回应了，pingNumber应该为0）
                    isConnect = false;
                    logError(threadName, "对方未回应心跳包，开始关闭Socket");

                    try {
                        socket.close();
                        logInfo(threadName, "Socket关闭成功");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        logError(threadName, "Socket关闭失败");
                    }

                    break;
                }

//                生成一个不为0的ping数
                do {
                    pingNumber = random.nextLong();
                } while (pingNumber == 0);

                VarInt packetId = new VarInt(0x20);
                VarLong l = new VarLong(pingNumber);
                Packet packet = new Packet(
                        new VarInt(packetId.getBytesLength() + l.getBytesLength()),
                        packetId,
                        l.getBytes()
                );
                sendQueue.add(packet);
            }

            cdl.countDown();
            logInfo(threadName, "结束工作");
        });

        //                                    如果不匹配则直接抛出，让下面的catch捕捉到
        Thread receiveHandlerThread = new Thread(() -> {
            String threadName = "接收处理";
            logInfo(threadName, "线程启动");
            while (isConnect) {
                try {
                    Packet packet = receiveQueue.poll();
                    if (packet != null) {
                        switch (packet.getId().getValue()) {
                            case 0x20:
//                                心跳包接收
                                VarLong ping = new VarLong(packet.getData());
                                if (ping.getValue() == pingNumber) {
                                    pingNumber = 0L;
                                } else {
//                                    如果不匹配则直接抛出，让下面的catch捕捉到
                                    logError(threadName, "心跳包回应错误，开始关闭Socket");
                                    isConnect = false;
                                    socket.close();
                                }
                                break;
                            case 0xF0:
//                                关闭包接收
                                VarIntString exitMsg = new VarIntString(packet.getData());
                                logInfo(threadName, "对方要求断开连接，原因：" + exitMsg.getContent());
                                isConnect = false;
                                socket.close();
                                break;
                            case 0x10:
//                                玩家加入游戏消息包接收
                                session.sendMessageFromMinecraftThread(
                                        this,
                                        ReplacePlaceholderUtil.replacePlaceholderWithString(
                                                joinFormatString.getContent(),
                                                MinecraftFormatPlaceholder.SERVER_NAME,
                                                serverName.getContent(),
                                                MinecraftFormatPlaceholder.PLAYER_NAME,
                                                new VarIntString(packet.getData()).getContent()
                                        )
                                );
                                break;
                            case 0x11:
//                                玩家离开游戏时消息包接收
                                session.sendMessageFromMinecraftThread(
                                        this,
                                        ReplacePlaceholderUtil.replacePlaceholderWithString(
                                                quitFormatString.getContent(),
                                                MinecraftFormatPlaceholder.SERVER_NAME,
                                                serverName.getContent(),
                                                MinecraftFormatPlaceholder.PLAYER_NAME,
                                                new VarIntString(packet.getData()).getContent()
                                        )
                                );
                                break;
                            case 0x12:
//                                玩家发送游戏消息时消息包接收
                                VarIntString pn12 = new VarIntString(packet.getData());
                                session.sendMessageFromMinecraftThread(
                                        this,
                                        ReplacePlaceholderUtil.replacePlaceholderWithString(
                                                msgFormatString.getContent(),
                                                MinecraftFormatPlaceholder.SERVER_NAME,
                                                serverName.getContent(),
                                                MinecraftFormatPlaceholder.PLAYER_NAME,
                                                pn12.getContent(),
                                                MinecraftFormatPlaceholder.MESSAGE,
                                                new VarIntString(Arrays.copyOfRange(
                                                        packet.getData(),
                                                        pn12.getBytesLength(),
                                                        packet.getData().length
                                                )).getContent()
                                        )
                                );
                                break;
                            case 0x13:
//                                玩家死亡时消息包接收
                                VarIntString pn13 = new VarIntString(packet.getData());
                                session.sendMessageFromMinecraftThread(
                                        this,
                                        ReplacePlaceholderUtil.replacePlaceholderWithString(
                                                deathFormatString.getContent(),
                                                MinecraftFormatPlaceholder.SERVER_NAME,
                                                serverName.getContent(),
                                                MinecraftFormatPlaceholder.PLAYER_NAME,
                                                pn13.getContent(),
                                                MinecraftFormatPlaceholder.DEATH_MESSAGE,
                                                new VarIntString(Arrays.copyOfRange(
                                                        packet.getData(),
                                                        pn13.getBytesLength(),
                                                        packet.getData().length
                                                )).getContent()
                                        )
                                );
                                break;
                            case 0x14:
//                                玩家被踢出游戏时消息包接收
                                VarIntString pn14 = new VarIntString(packet.getData());
                                session.sendMessageFromMinecraftThread(
                                        this,
                                        ReplacePlaceholderUtil.replacePlaceholderWithString(
                                                kickFormatString.getContent(),
                                                MinecraftFormatPlaceholder.SERVER_NAME,
                                                serverName.getContent(),
                                                MinecraftFormatPlaceholder.PLAYER_NAME,
                                                pn14.getContent(),
                                                MinecraftFormatPlaceholder.KICK_REASON,
                                                new VarIntString(Arrays.copyOfRange(
                                                        packet.getData(),
                                                        pn14.getBytesLength(),
                                                        packet.getData().length
                                                )).getContent()
                                        )
                                );
                                break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    isConnect = false;
                    logError(threadName, "出现异常，开始关闭Socket");

                    try {
                        socket.close();
                        logInfo(threadName, "Socket关闭成功");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        logError(threadName, "Socket关闭失败");
                    }
                }
            }

            cdl.countDown();
            logInfo(threadName, "结束工作");
        });

//        启动线程
        sendThread.start();
        receiveThread.start();
        heartbreakThread.start();
        receiveHandlerThread.start();

//        等待所有线程结束
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logInfo("总线程", "所有线程均已停止工作");

//        确认socket已关闭，否则执行关闭
        while (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        logInfo("Socket", "已确认关闭");

//        从对话撤出该线程
        session.delMinecraftThread(this);
        logInfo("总线程", "已撤除该线程");
    }

    public void addSendQueue(Packet packet) {
        sendQueue.add(packet);
    }

    public VarLong getSessionId() {
        return sessionId;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public VarIntString getServerName() {
        return serverName;
    }

    public MinecraftConnectionThread(Socket socket, VarLong sessionId, VarIntString serverName, VarIntString joinFormatString, VarIntString quitFormatString, VarIntString msgFormatString, VarIntString deathFormatString, VarIntString kickFormatString) throws IOException {
        this.socket = socket;
        this.sessionId = sessionId;
        this.serverName = serverName;
        this.joinFormatString = joinFormatString;
        this.quitFormatString = quitFormatString;
        this.msgFormatString = msgFormatString;
        this.deathFormatString = deathFormatString;
        this.kickFormatString = kickFormatString;

        this.inputStream = new BufferedInputStream(socket.getInputStream());
        this.outputStream = new BufferedOutputStream(socket.getOutputStream());

        this.serverAddress = socket.getRemoteSocketAddress().toString();
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
