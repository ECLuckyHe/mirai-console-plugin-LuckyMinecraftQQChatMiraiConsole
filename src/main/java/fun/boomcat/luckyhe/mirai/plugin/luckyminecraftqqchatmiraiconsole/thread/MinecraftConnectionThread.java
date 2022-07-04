package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.thread;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.config.ConfigOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.datatype.VarInt;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.datatype.VarIntString;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.datatype.VarLong;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.pojo.Packet;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.util.ByteUtil;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.util.ConnectionPacketReceiveUtil;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.Session;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.*;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.MiraiLogger;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class MinecraftConnectionThread extends Thread {
    private final VarLong sessionId;
    private Session session;
    private final VarIntString serverName;
    private final VarIntString joinFormatString;
    private final VarIntString quitFormatString;
    private final VarIntString msgFormatString;
    private final VarIntString deathFormatString;
    private final VarIntString kickFormatString;
    private final VarIntString[] onlinePlayersCommands;
    private final VarIntString onlinePlayersCommandResponseFormat;
    private final VarIntString onlinePlayersCommandResponseSeparator;
    private final VarIntString rconCommandPrefix;
    private final VarIntString rconCommandResultFormat;
    private final VarIntString userCommandPrefix;
    private final VarIntString userBindPrefix;
    private final VarIntString[] getUserCommandsCommands;
    private final VarIntString whitelistCorrectMessage;

    private final VarIntString whitelistTryMessage;

    //    断开原因
    private String disconnectReason = "异常退出";

    private final String serverAddress;

    private boolean isConnected = true;

    private final MiraiLogger logger = MiraiLoggerUtil.getLogger();

    private final LinkedBlockingQueue<Packet> sendQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Packet> receiveQueue = new LinkedBlockingQueue<>();
    private final Queue<Long> pingQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Long> onlinePlayersCommandSendGroupQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Long> rconCommandSendGroupQueue = new ConcurrentLinkedQueue<>();
    //    添加用户指令的队列
    private final Queue<Long> addUserCommandQueue = new ConcurrentLinkedQueue<>();
    //    删除用户指令的队列
    private final Queue<Long> delUserCommandQueue = new ConcurrentLinkedQueue<>();
    //    获取用户指令（mcchat指令）队列
    private final Queue<Long> getMcChatUserCommandsQueue = new ConcurrentLinkedQueue<>();
    //    绑定qq和mcid队列（群号）
    private final Queue<Long> userBindQueue = new ConcurrentLinkedQueue<>();
    //    发送用户指令队列（群号）
    private final Queue<Long> userCommandGroupQueue = new ConcurrentLinkedQueue<>();
    //    获取用户指令（普通用户）队列
    private final Queue<Long> getUserCommandsGroupQueue = new ConcurrentLinkedQueue<>();

    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    private final CountDownLatch noTakeQueueThreadCdl = new CountDownLatch(2);
    private final CountDownLatch cdl = new CountDownLatch(4);

    public String getStringWithPrefix(String threadName, String info) {
        return "[" + session.getId() + "(" + session.getName() + ")][" + serverName.getContent() + serverAddress + "]" +
                "[" + threadName + "] " + info;
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

    public void sendMessage(
            Group group,
            Member member,
            String groupNickname,
            MessageChain message
    ) {
        VarInt packetId = new VarInt(0x10);
        VarLong gi = new VarLong(group.getId());
        VarIntString gn = new VarIntString(group.getName());
        VarIntString gnm = new VarIntString(groupNickname);
        VarLong si = new VarLong(member.getId());
        VarIntString snm = new VarIntString(member.getNick());
        VarIntString sgnm = new VarIntString(member.getNameCard().length() == 0 ? member.getNick() : member.getNameCard());

        int len = packetId.getBytesLength() + gi.getBytesLength() +
                gn.getBytesLength() + gnm.getBytesLength() +
                si.getBytesLength() + snm.getBytesLength() +
                sgnm.getBytesLength();

        byte[] data = ByteUtil.byteMergeAll(
                gi.getBytes(),
                gn.getBytes(),
                gnm.getBytes(),
                si.getBytes(),
                snm.getBytes(),
                sgnm.getBytes()
        );

//        处理消息
        List<byte[]> messageBytes = new ArrayList<>();
        for (SingleMessage singleMessage : message) {
            if (singleMessage instanceof At) {
                messageBytes.add(ByteUtil.byteMergeAll(
                        new byte[] {0x01},
                        new VarLong(((At) singleMessage).getTarget()).getBytes()
                ));
            } else if (singleMessage instanceof AtAll) {
                messageBytes.add(ByteUtil.byteMergeAll(new byte[] {0x02}));
            } else if (singleMessage instanceof Image && singleMessage.contentToString().equals("[动画表情]")) {
                messageBytes.add(ByteUtil.byteMergeAll(
                        new byte[] {0x05},
                        new VarIntString(Image.queryUrl(((Image) singleMessage))).getBytes()
                ));
            } else if (singleMessage instanceof Image && singleMessage.contentToString().equals("[图片]")) {
                messageBytes.add(ByteUtil.byteMergeAll(
                        new byte[] {0x03},
                        new VarIntString(Image.queryUrl(((Image) singleMessage))).getBytes()
                ));
            } else if (singleMessage instanceof QuoteReply) {
                QuoteReply quoteReply = (QuoteReply) singleMessage;
                messageBytes.add(ByteUtil.byteMergeAll(
                        new byte[] {0x04},
                        new VarLong(quoteReply.getSource().getFromId()).getBytes(),
                        new VarIntString(quoteReply.getSource().getOriginalMessage().contentToString()).getBytes()
                ));
            } else {
//                其他，长度为0则不发
                String s = singleMessage.contentToString();
                if (s.length() == 0) {
                    continue;
                }

                messageBytes.add(ByteUtil.byteMergeAll(
                        new byte[] {0x00},
                        new VarIntString(s).getBytes()
                ));
            }
        }

//        组装消息
        VarInt messageBytesLength = new VarInt(messageBytes.size());
        len += messageBytesLength.getBytesLength();
        data = ByteUtil.byteMergeAll(data, messageBytesLength.getBytes());

        for (byte[] bytes : messageBytes) {
            len += bytes.length;
            data = ByteUtil.byteMergeAll(data, bytes);
        }

        addSendQueue(new Packet(
                new VarInt(len),
                packetId,
                data
        ));
    }

    public synchronized void sendGetOnlinePlayersPacket(long groupId) {
//        同步方法防止顺序错误
        onlinePlayersCommandSendGroupQueue.add(groupId);
        VarInt packetId = new VarInt(0x21);
        addSendQueue(new Packet(new VarInt(packetId.getBytesLength()), packetId, new byte[]{}));
    }

    public synchronized void sendRconCommandPacket(long groupId, long senderId, String command) {
//        发送rcon指令
        rconCommandSendGroupQueue.add(groupId);
        VarInt packetId = new VarInt(0x22);
        VarLong senderIdLong = new VarLong(senderId);
        VarIntString commandString = new VarIntString(command);
        addSendQueue(new Packet(
                new VarInt(packetId.getBytesLength() + senderIdLong.getBytesLength() + commandString.getBytesLength()),
                packetId,
                ByteUtil.byteMergeAll(senderIdLong.getBytes(), commandString.getBytes())
        ));
    }

    public synchronized void sendAnnouncementPacket(long senderId, String senderNickname, String announcement) {
//        发送公告
        VarInt packetId = new VarInt(0x23);
        VarLong senderIdLong = new VarLong(senderId);
        VarIntString senderNicknameString = new VarIntString(senderNickname);
        VarIntString announcementString = new VarIntString(announcement);

        addSendQueue(new Packet(
                new VarInt(packetId.getBytesLength() + senderIdLong.getBytesLength() + senderNicknameString.getBytesLength() + announcementString.getBytesLength()),
                packetId,
                ByteUtil.byteMergeAll(senderIdLong.getBytes(), senderNicknameString.getBytes(), announcementString.getBytes())
        ));
    }

    public synchronized void sendUserCommandPacket(long senderId, long groupId, String command) {
//        发送用户指令
        userCommandGroupQueue.add(groupId);
        VarInt packetId = new VarInt(0x24);
        VarLong senderIdLong = new VarLong(senderId);
        VarIntString commandString = new VarIntString(command);

        addSendQueue(new Packet(
                new VarInt(packetId.getBytesLength() + senderIdLong.getBytesLength() + commandString.getBytesLength()),
                packetId,
                ByteUtil.byteMergeAll(senderIdLong.getBytes(), commandString.getBytes())
        ));
    }

    public synchronized void sendAddUserCommandPacket(long senderId, String name, String userCommand, String mapCommand) {
//        添加用户指令
        addUserCommandQueue.add(senderId);
        VarInt packetId = new VarInt(0x25);
        VarLong senderIdLong = new VarLong(senderId);
        VarIntString nameString = new VarIntString(name);
        VarIntString userCommandString = new VarIntString(userCommand);
        VarIntString mapCommandString = new VarIntString(mapCommand);

        addSendQueue(new Packet(
                new VarInt(packetId.getBytesLength() +
                        senderIdLong.getBytesLength() +
                        nameString.getBytesLength() +
                        userCommandString.getBytesLength() +
                        mapCommandString.getBytesLength()),
                packetId,
                ByteUtil.byteMergeAll(
                        senderIdLong.getBytes(),
                        nameString.getBytes(),
                        userCommandString.getBytes(),
                        mapCommandString.getBytes()
                )
        ));
    }

    public synchronized void sendUserBindPacket(long groupId, long senderId, String mcid) {
//        绑定qq和mcid
        userBindQueue.add(groupId);
        VarInt packetId = new VarInt(0x28);
        VarLong senderIdLong = new VarLong(senderId);
        VarIntString mcidString = new VarIntString(mcid);

        addSendQueue(new Packet(
                new VarInt(packetId.getBytesLength() + senderIdLong.getBytesLength() + mcidString.getBytesLength()),
                packetId,
                ByteUtil.byteMergeAll(senderIdLong.getBytes(), mcidString.getBytes())
        ));
    }

    public synchronized void sendDelUserCommandPacket(long senderId, String name) {
//        删除用户指令
        delUserCommandQueue.add(senderId);
        VarInt packetId = new VarInt(0x26);
        VarLong senderIdLong = new VarLong(senderId);
        VarIntString nameString = new VarIntString(name);
        addSendQueue(new Packet(
                new VarInt(packetId.getBytesLength() +
                        senderIdLong.getBytesLength() +
                        nameString.getBytesLength()),
                packetId,
                ByteUtil.byteMergeAll(
                        senderIdLong.getBytes(),
                        nameString.getBytes()
                )
        ));
    }

    public synchronized void sendGetMcChatUserCommands(long senderId) {
//        获取用户指令列表（mcchat指令）
        getMcChatUserCommandsQueue.add(senderId);
        VarInt packetId = new VarInt(0x27);
        addSendQueue(new Packet(
                new VarInt(packetId.getBytesLength()),
                packetId,
                new byte[]{}
        ));
    }

    public synchronized void sendGetUserCommands(long groupId) {
//        获取用户指令列表（普通用户）
        getUserCommandsGroupQueue.add(groupId);
        VarInt packetId = new VarInt(0x29);
        addSendQueue(new Packet(
                new VarInt(packetId.getBytesLength()),
                packetId,
                new byte[]{}
        ));
    }

    @Override
    public void run() {
        List<Object> pingRight = new ArrayList<>();
//        发送线程，负责从队列取数据包发送
//        发送前先确认，若为关闭包，则发送后关闭socket
        AsyncCaller.run(() -> {
            String threadName = "发送";
            logInfo(threadName, "线程启动");
            while (isConnected) {
                try {
                    Packet packet = sendQueue.take();
                    outputStream.write(packet.getBytes());
                    outputStream.flush();

//                    如果包id为-1，则进行下一轮循环
                    if (packet.getId().getValue() == -1) {
                        continue;
                    }

//                        如果发送数据包为关闭包
                    if (packet.getId().getValue() == 0xF0) {
                        VarIntString reason = new VarIntString(packet.getData());
                        logInfo(threadName, "发送关闭包，内容：" + reason.getContent());
                        disconnectReason = "发送关闭包，内容：" + reason.getContent();
                        isConnected = false;
                        socket.close();
                    }
                } catch (Exception e) {
                    isConnected = false;
//                    e.printStackTrace();
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

//        接收线程
//        将接收的数据放入到队列中
        AsyncCaller.run(() -> {
            String threadName = "接收";
            logInfo(threadName, "线程启动");
            while (isConnected) {
                try {
                    Packet packet = ConnectionPacketReceiveUtil.getPacket(inputStream);
                    receiveQueue.add(packet);

                } catch (Exception e) {
                    isConnected = false;
//                    e.printStackTrace();
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
            noTakeQueueThreadCdl.countDown();
            logInfo(threadName, "结束工作");
        });

//        心跳线程
//        发送心跳包和检测上一次发送的心跳包
        AsyncCaller.run(() -> {
            String threadName = "心跳";
            logInfo(threadName, "线程启动");
            Random random = new Random();
//            该标记用于表示是否为第一次心跳包，主要是为了进入循环后马上发送一次心跳包
            boolean theFirstTime = true;
            while (isConnected) {
                try {
                    for (int i = 0; (!theFirstTime) && i < ConfigOperation.getHeartbeat() && isConnected; i++) {
                        Thread.sleep(1000L);
                    }
                    if (theFirstTime) {
                        theFirstTime = false;
                    }
                } catch (Exception e) {
                    isConnected = false;
//                    e.printStackTrace();
                    logError(threadName, "线程异常停止，开始关闭Socket");

                    try {
                        socket.close();
                        logInfo(threadName, "Socket关闭成功");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        logError(threadName, "Socket关闭失败");
                    }
                }

                if (pingQueue.size() >= 3) {
                    isConnected = false;

                    logError(threadName, "对方已经连续未回应" + pingQueue.size() + "次心跳包，开始关闭Socket");
                    disconnectReason = "对方已经连续未回应" + pingQueue.size() + "次心跳包";

                    try {
                        socket.close();
                        logInfo(threadName, "Socket关闭成功");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        logError(threadName, "Socket关闭失败");
                    }
                }

//                生成一个不为0和1的ping数
                long pingNumber;
                do {
                    pingNumber = random.nextLong();
                } while (pingNumber == 0 || pingNumber == 1);

                pingQueue.add(pingNumber);

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
            noTakeQueueThreadCdl.countDown();
            logInfo(threadName, "结束工作");
        });

//        接收处理线程
//        从队列中取出接收的内容并处理
        AsyncCaller.run(() -> {
            String threadName = "接收处理";
            logInfo(threadName, "线程启动");
//            是否是第一次接收到心跳包的标记
            boolean isFirstTime = true;
            cycle:
            while (isConnected) {
                try {
                    Packet packet = receiveQueue.take();
                    switch (packet.getId().getValue()) {
                        case -1:
//                            包id为-1则进行下一轮
                            continue;
                        case 0x20:
//                                心跳包接收
                            VarLong ping = new VarLong(packet.getData());
                            Long sent = pingQueue.poll();
                            if (sent == null) {
//                                    没有发心跳包却收到了心跳包
                                logError(threadName, "并未发送心跳包但收到了回应，开始关闭Socket");
                                isConnected = false;
                                socket.close();
                                break;
                            }

                            if (!(ping.getValue() == sent + ConfigOperation.getHeartbeat())) {
//                                    如果不匹配则断开连接
                                logError(threadName, "心跳包回应错误，开始关闭Socket");
                                isConnected = false;
                                socket.close();
                                break cycle;
                            }

                            if (isFirstTime) {
                                //            向群内公告此连接
                                session.sendMessageToAllGroups(new PlainText("有Minecraft服务端接入会话！\n会话名：" + session.getName() + "\n" +
                                        "服务端名称：" + serverName.getContent() + "\n" +
                                        "地址：" + serverAddress + "\n" +
                                        "时间：" + new Date()));
                                isFirstTime = false;
                                pingRight.add(new Object());

                                VarInt confirmPacketId = new VarInt(0x20);
                                VarLong value = new VarLong(1);
                                sendQueue.add(new Packet(
                                        new VarInt(confirmPacketId.getBytesLength() + value.getBytesLength()),
                                        confirmPacketId,
                                        value.getBytes()
                                ));
                            }

                            break;
                        case 0xF0:
//                                关闭包接收
                            VarIntString exitMsg = new VarIntString(packet.getData());
                            logInfo(threadName, "对方要求断开连接，原因：" + exitMsg.getContent());
                            disconnectReason = "对方要求断开连接，原因：" + exitMsg.getContent();
                            isConnected = false;
                            socket.close();
                            break;
                        case 0x10:
//                                玩家加入游戏消息包接收
                            session.sendMessageFromMinecraftThread(
                                    this,
                                    MessageUtil.getColorCodeRemoved(Objects.requireNonNull(ReplacePlaceholderUtil.replacePlaceholderWithString(
                                            joinFormatString.getContent(),
                                            MinecraftFormatPlaceholder.SERVER_NAME,
                                            serverName.getContent(),
                                            MinecraftFormatPlaceholder.PLAYER_NAME,
                                            new VarIntString(packet.getData()).getContent()
                                    )))
                            );
                            break;
                        case 0x11:
//                                玩家离开游戏时消息包接收
                            session.sendMessageFromMinecraftThread(
                                    this,
                                    MessageUtil.getColorCodeRemoved(Objects.requireNonNull(ReplacePlaceholderUtil.replacePlaceholderWithString(
                                            quitFormatString.getContent(),
                                            MinecraftFormatPlaceholder.SERVER_NAME,
                                            serverName.getContent(),
                                            MinecraftFormatPlaceholder.PLAYER_NAME,
                                            new VarIntString(packet.getData()).getContent()
                                    )))
                            );
                            break;
                        case 0x12:
//                                玩家发送游戏消息时消息包接收
                            VarIntString pn12 = new VarIntString(packet.getData());
                            session.sendMessageFromMinecraftThread(
                                    this,
                                    MessageUtil.getColorCodeRemoved(Objects.requireNonNull(ReplacePlaceholderUtil.replacePlaceholderWithString(
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
                                    )))
                            );
                            break;
                        case 0x13:
//                                玩家死亡时消息包接收
                            VarIntString pn13 = new VarIntString(packet.getData());
                            session.sendMessageFromMinecraftThread(
                                    this,
                                    MessageUtil.getColorCodeRemoved(Objects.requireNonNull(ReplacePlaceholderUtil.replacePlaceholderWithString(
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
                                    )))
                            );
                            break;
                        case 0x14:
//                                玩家被踢出游戏时消息包接收
                            VarIntString pn14 = new VarIntString(packet.getData());
                            session.sendMessageFromMinecraftThread(
                                    this,
                                    MessageUtil.getColorCodeRemoved(Objects.requireNonNull(ReplacePlaceholderUtil.replacePlaceholderWithString(
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
                                    )))
                            );
                            break;
                        case 0x21:
//                                收到在线玩家信息数据包时
                            Long getOnlineGroup = onlinePlayersCommandSendGroupQueue.poll();
                            if (getOnlineGroup == null) {
//                                    没有人发送获取在线玩家列表数据包却收到了
                                logError(threadName, "没有人发送获取在线玩家列表数据包但却收到了玩家列表数据包，开始关闭Socket");
                                isConnected = false;
                                socket.close();
                                break;
                            }

                            VarInt onlinePlayers = new VarInt(packet.getData());
                            int len = onlinePlayers.getBytesLength();

                            List<VarIntString> playerIds = new ArrayList<>();
                            for (int i = 0; i < onlinePlayers.getValue(); i++) {
                                VarIntString player = new VarIntString(Arrays.copyOfRange(packet.getData(), len, packet.getData().length));
                                len += player.getBytesLength();
                                playerIds.add(player);
                            }

                            StringBuilder playerIdsString = new StringBuilder();
                            for (int i = 0; i < playerIds.size(); i++) {
                                playerIdsString.append(playerIds.get(i).getContent());
                                if (i != playerIds.size() - 1) {
                                    playerIdsString.append(onlinePlayersCommandResponseSeparator.getContent());
                                }
                            }

                            String res = ReplacePlaceholderUtil.replacePlaceholderWithString(
                                    onlinePlayersCommandResponseFormat.getContent(),
                                    MinecraftFormatPlaceholder.SERVER_NAME,
                                    serverName.getContent(),
                                    MinecraftFormatPlaceholder.PLAYERS,
                                    playerIdsString.toString(),
                                    MinecraftFormatPlaceholder.COUNT,
                                    String.valueOf(playerIds.size())
                            );

//                                发送消息到群内
                            session.sendMessageToGroup(getOnlineGroup, new PlainText(Objects.requireNonNull(res)));
                            break;

                        case 0x22:
//                                指令执行结果
                            VarIntString commandRes = new VarIntString(packet.getData());
                            Long commandGroupId = rconCommandSendGroupQueue.poll();
                            if (commandGroupId == null) {
//                                    没有人发送指令但却收到了指令回复
                                logError(threadName, "没有人发送管理员指令但却收到了指令回复包，开始关闭Socket");
                                isConnected = false;
                                socket.close();
                                break;
                            }

                            session.sendMessageToGroup(commandGroupId, new PlainText(MessageUtil.getColorCodeRemoved(Objects.requireNonNull(ReplacePlaceholderUtil.replacePlaceholderWithString(
                                    rconCommandResultFormat.getContent(),
                                    MinecraftFormatPlaceholder.SERVER_NAME,
                                    serverName.getContent(),
                                    MinecraftFormatPlaceholder.RESULT,
                                    commandRes.getContent()
                            )))));
                            break;

                        case 0x24: {
//                            用户指令返回结果
                            VarIntString commandResult = new VarIntString(packet.getData());
                            Long groupId = userCommandGroupQueue.poll();
                            if (groupId == null) {
//                                    没有人发送指令但却收到了指令回复
                                logError(threadName, "没有人发送用户指令但却收到了用户指令回复包，开始关闭Socket");
                                isConnected = false;
                                socket.close();
                                break;
                            }

                            session.sendMessageToGroup(groupId, new PlainText(MessageUtil.getColorCodeRemoved(Objects.requireNonNull(ReplacePlaceholderUtil.replacePlaceholderWithString(
                                    rconCommandResultFormat.getContent(),
                                    MinecraftFormatPlaceholder.SERVER_NAME,
                                    serverName.getContent(),
                                    MinecraftFormatPlaceholder.RESULT,
                                    commandResult.getContent()
                            )))));


                            break;
                        }

                        case 0x25: {
//                            添加用户指令返回
                            VarIntString msg = new VarIntString(packet.getData());
                            Long id = addUserCommandQueue.poll();

                            if (id == null) {
                                logError(threadName, "没有人发送添加用户指令但却收到了添加用户指令回复包，开始关闭Socket");
                                isConnected = false;
                                socket.close();
                                break;
                            }

                            session.sendMessageToFriend(id, new PlainText("[异步消息] " + msg.getContent()));

                            break;
                        }

                        case 0x26: {
//                            删除用户指令返回
                            VarIntString msg = new VarIntString(packet.getData());
                            Long id = delUserCommandQueue.poll();

                            if (id == null) {
                                logError(threadName, "没有人发送删除用户指令但却收到了删除用户指令回复包，开始关闭Socket");
                                isConnected = false;
                                socket.close();
                                break;
                            }

                            session.sendMessageToFriend(id, new PlainText("[异步消息] " + msg.getContent()));

                            break;
                        }

                        case 0x27:
                        case 0x29: {
//                            获取用户指令列表
                            Long qq = null;
                            Long groupId = null;
                            if (packet.getId().getValue() == 0x27) {
//                                mcchat获取
                                qq = getMcChatUserCommandsQueue.poll();
                                if (qq == null) {
                                    logError(threadName, "没有人发送获取用户指令（mcchat指令）列表包但是却收到了，开始关闭Socket");
                                    isConnected = false;
                                    socket.close();
                                    break;
                                }
                            }

                            if (packet.getId().getValue() == 0x29) {
//                                普通用户获取
                                groupId = getUserCommandsGroupQueue.poll();
                                if (groupId == null) {
                                    logError(threadName, "没有人发送获取用户指令（普通用户）列表包但是却收到了，开始关闭Socket");
                                    isConnected = false;
                                    socket.close();
                                    break;
                                }
                            }

                            byte[] data = packet.getData();
                            int i = 0;
                            VarInt commandLength = new VarInt(Arrays.copyOfRange(data, i, data.length));
                            i += commandLength.getBytesLength();

                            List<Map<String, String>> commandMaps = new ArrayList<>();

                            for (int j = 0; j < commandLength.getValue(); j++) {
                                VarIntString name = new VarIntString(Arrays.copyOfRange(data, i, data.length));
                                i += name.getBytesLength();
                                VarIntString command = new VarIntString(Arrays.copyOfRange(data, i, data.length));
                                i += command.getBytesLength();
                                VarIntString mapping = new VarIntString(Arrays.copyOfRange(data, i, data.length));
                                i += mapping.getBytesLength();

                                Map<String, String> newMap = new HashMap<>();
                                newMap.put("name", name.getContent());
                                newMap.put("command", command.getContent());
                                newMap.put("mapping", mapping.getContent());

                                commandMaps.add(newMap);
                            }

                            if (packet.getId().getValue() == 0x27) {
                                session.sendMessageToFriend(qq, UserCommandUtil.getForwardMessage(
                                        Bot.getInstances().get(0),
                                        Bot.getInstances().get(0).getFriendOrFail(qq),
                                        commandMaps,
                                        null
                                ));
                            }

                            if (packet.getId().getValue() == 0x29) {
                                session.sendMessageToGroup(groupId, UserCommandUtil.getForwardMessage(
                                        Bot.getInstances().get(0),
                                        Bot.getInstances().get(0).getGroupOrFail(groupId),
                                        commandMaps,
                                        this
                                ));
                            }

                            break;
                        }

                        case 0x28: {
//                            绑定mcid和qq返回结果
                            Long groupId = userBindQueue.poll();
                            if (groupId == null) {
                                logError(threadName, "没有人发送绑定用户数据包但却收到了绑定用户回复包，开始关闭Socket");
                                isConnected = false;
                                socket.close();
                                break;
                            }

                            VarIntString msg = new VarIntString(packet.getData());

                            session.sendMessageToGroup(groupId, new PlainText(msg.getContent()));

                            break;
                        }

                        case 0x30: {
//                            非白名单尝试进入
                            VarIntString playerName = new VarIntString(packet.getData());
                            session.sendMessageToAllGroups(new PlainText(Objects.requireNonNull(ReplacePlaceholderUtil.replacePlaceholderWithString(
                                    whitelistTryMessage.getContent(),
                                    MinecraftFormatPlaceholder.SERVER_NAME,
                                    serverName.getContent(),
                                    MinecraftFormatPlaceholder.PLAYER_NAME,
                                    playerName.getContent()
                            ))));
                            break;
                        }

                        case 0x31: {
//                            非白名单尝试进入
                            VarIntString playerName = new VarIntString(packet.getData());
                            session.sendMessageToAllGroups(new PlainText(Objects.requireNonNull(ReplacePlaceholderUtil.replacePlaceholderWithString(
                                    whitelistCorrectMessage.getContent(),
                                    MinecraftFormatPlaceholder.SERVER_NAME,
                                    serverName.getContent(),
                                    MinecraftFormatPlaceholder.PLAYER_NAME,
                                    playerName.getContent()
                            ))));
                            break;
                        }
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                    isConnected = false;
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

//        等待 非取队列线程 结束
        try {
            noTakeQueueThreadCdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        加入空包，以使 取队列线程 进行下一轮循环并退出
        sendQueue.add(new Packet(new VarInt(0), new VarInt(-1), null));
        receiveQueue.add(new Packet(new VarInt(0), new VarInt(-1), null));

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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        logInfo("Socket", "已确认关闭");

//        从对话撤出该线程
        session.delMinecraftThread(this);
        logInfo("总线程", "已撤除该连接");

//        发送断开连接消息
        if (pingRight.size() == 1) {
            session.sendMessageToAllGroups(new PlainText("有Minecraft服务端断开会话！\n会话名：" +
                    session.getName() + "\n服务端名称：" +
                    serverName.getContent() + "\n地址：" +
                    serverAddress + "\n原因：" +
                    disconnectReason + "\n时间：" + new Date()));
        }
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

    public VarIntString[] getOnlinePlayersCommands() {
        return onlinePlayersCommands;
    }

    public VarIntString[] getGetUserCommandsCommands() {
        return getUserCommandsCommands;
    }

    public VarIntString getRconCommandPrefix() {
        return rconCommandPrefix;
    }

    public VarIntString getUserCommandPrefix() {
        return userCommandPrefix;
    }

    public VarIntString getUserBindPrefix() {
        return userBindPrefix;
    }

    public VarIntString getJoinFormatString() {
        return joinFormatString;
    }

    public VarIntString getQuitFormatString() {
        return quitFormatString;
    }

    public VarIntString getMsgFormatString() {
        return msgFormatString;
    }

    public VarIntString getDeathFormatString() {
        return deathFormatString;
    }

    public VarIntString getKickFormatString() {
        return kickFormatString;
    }

    public MinecraftConnectionThread(
            Socket socket,
            VarLong sessionId,
            VarIntString serverName,
            VarIntString joinFormatString,
            VarIntString quitFormatString,
            VarIntString msgFormatString,
            VarIntString deathFormatString,
            VarIntString kickFormatString,
            VarIntString[] onlinePlayersCommands,
            VarIntString onlinePlayersCommandResponseFormat,
            VarIntString onlinePlayersCommandResponseSeparator,
            VarIntString rconCommandPrefix,
            VarIntString rconCommandResultFormat,
            VarIntString userCommandPrefix,
            VarIntString userBindPrefix,
            VarIntString[] getUserCommandsCommands,
            VarIntString whitelistCorrectMessage,
            VarIntString whitelistTryMessage
    ) throws IOException {
        this.socket = socket;
        this.sessionId = sessionId;
        this.serverName = serverName;
        this.joinFormatString = joinFormatString;
        this.quitFormatString = quitFormatString;
        this.msgFormatString = msgFormatString;
        this.deathFormatString = deathFormatString;
        this.kickFormatString = kickFormatString;
        this.onlinePlayersCommands = onlinePlayersCommands;
        this.onlinePlayersCommandResponseFormat = onlinePlayersCommandResponseFormat;
        this.onlinePlayersCommandResponseSeparator = onlinePlayersCommandResponseSeparator;
        this.rconCommandPrefix = rconCommandPrefix;
        this.rconCommandResultFormat = rconCommandResultFormat;
        this.userCommandPrefix = userCommandPrefix;
        this.userBindPrefix = userBindPrefix;
        this.getUserCommandsCommands = getUserCommandsCommands;
        this.whitelistCorrectMessage = whitelistCorrectMessage;
        this.whitelistTryMessage = whitelistTryMessage;

        this.inputStream = new BufferedInputStream(socket.getInputStream());
        this.outputStream = new BufferedOutputStream(socket.getOutputStream());

        this.serverAddress = socket.getRemoteSocketAddress().toString();
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
