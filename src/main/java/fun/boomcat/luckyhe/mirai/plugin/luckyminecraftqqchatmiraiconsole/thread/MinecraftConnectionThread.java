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

    //    ????????????
    private String disconnectReason = "????????????";

    private final String serverAddress;

    private boolean isConnected = true;

    private final MiraiLogger logger = MiraiLoggerUtil.getLogger();

    private final LinkedBlockingQueue<Packet> sendQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Packet> receiveQueue = new LinkedBlockingQueue<>();
    private final Queue<Long> pingQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Long> onlinePlayersCommandSendGroupQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Long> rconCommandSendGroupQueue = new ConcurrentLinkedQueue<>();
    //    ???????????????????????????
    private final Queue<Long> addUserCommandQueue = new ConcurrentLinkedQueue<>();
    //    ???????????????????????????
    private final Queue<Long> delUserCommandQueue = new ConcurrentLinkedQueue<>();
    //    ?????????????????????mcchat???????????????
    private final Queue<Long> getMcChatUserCommandsQueue = new ConcurrentLinkedQueue<>();
    //    ??????qq???mcid??????????????????
    private final Queue<Long> userBindQueue = new ConcurrentLinkedQueue<>();
    //    ????????????????????????????????????
    private final Queue<Long> userCommandGroupQueue = new ConcurrentLinkedQueue<>();
    //    ??????????????????????????????????????????
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
            long groupId,
            String groupName,
            String groupNickname,
            long senderId,
            String senderNickname,
            String senderGroupNickname,
            MessageChain message
    ) {
        VarInt packetId = new VarInt(0x10);
        VarLong gi = new VarLong(groupId);
        VarIntString gn = new VarIntString(groupName);
        VarIntString gnm = new VarIntString(groupNickname);
        VarLong si = new VarLong(senderId);
        VarIntString snm = new VarIntString(senderNickname);
        VarIntString sgnm = new VarIntString(senderGroupNickname.length() == 0 ? senderNickname : senderGroupNickname);

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

//        ????????????
        List<byte[]> messageBytes = new ArrayList<>();
        for (SingleMessage singleMessage : message) {
            if (singleMessage instanceof At) {
                messageBytes.add(ByteUtil.byteMergeAll(
                        new byte[] {0x01},
                        new VarLong(((At) singleMessage).getTarget()).getBytes()
                ));
            } else if (singleMessage instanceof AtAll) {
                messageBytes.add(ByteUtil.byteMergeAll(new byte[] {0x02}));
            } else if (singleMessage instanceof Image && singleMessage.contentToString().equals("[????????????]")) {
                messageBytes.add(ByteUtil.byteMergeAll(
                        new byte[] {0x05},
                        new VarIntString(Image.queryUrl(((Image) singleMessage))).getBytes()
                ));
            } else if (singleMessage instanceof Image && singleMessage.contentToString().equals("[??????]")) {
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
//                ??????????????????0?????????
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

//        ????????????
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
//        ??????????????????????????????
        onlinePlayersCommandSendGroupQueue.add(groupId);
        VarInt packetId = new VarInt(0x21);
        addSendQueue(new Packet(new VarInt(packetId.getBytesLength()), packetId, new byte[]{}));
    }

    public synchronized void sendRconCommandPacket(long groupId, long senderId, String command) {
//        ??????rcon??????
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
//        ????????????
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
//        ??????????????????
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
//        ??????????????????
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
//        ??????qq???mcid
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
//        ??????????????????
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
//        ???????????????????????????mcchat?????????
        getMcChatUserCommandsQueue.add(senderId);
        VarInt packetId = new VarInt(0x27);
        addSendQueue(new Packet(
                new VarInt(packetId.getBytesLength()),
                packetId,
                new byte[]{}
        ));
    }

    public synchronized void sendGetUserCommands(long groupId) {
//        ??????????????????????????????????????????
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
//        ????????????????????????????????????????????????
//        ?????????????????????????????????????????????????????????socket
        AsyncCaller.run(() -> {
            String threadName = "??????";
            logInfo(threadName, "????????????");
            while (isConnected) {
                try {
                    Packet packet = sendQueue.take();
                    outputStream.write(packet.getBytes());
                    outputStream.flush();

//                    ?????????id???-1???????????????????????????
                    if (packet.getId().getValue() == -1) {
                        continue;
                    }

//                        ?????????????????????????????????
                    if (packet.getId().getValue() == 0xF0) {
                        VarIntString reason = new VarIntString(packet.getData());
                        logInfo(threadName, "???????????????????????????" + reason.getContent());
                        disconnectReason = "???????????????????????????" + reason.getContent();
                        isConnected = false;
                        socket.close();
                    }
                } catch (Exception e) {
                    isConnected = false;
//                    e.printStackTrace();
                    logError(threadName, "?????????????????????????????????Socket");

                    try {
                        socket.close();
                        logInfo(threadName, "Socket????????????");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        logError(threadName, "Socket????????????");
                    }
                }
            }
            cdl.countDown();
            logInfo(threadName, "????????????");
        });

//        ????????????
//        ????????????????????????????????????
        AsyncCaller.run(() -> {
            String threadName = "??????";
            logInfo(threadName, "????????????");
            while (isConnected) {
                try {
                    Packet packet = ConnectionPacketReceiveUtil.getPacket(inputStream);
                    receiveQueue.add(packet);

                } catch (Exception e) {
                    isConnected = false;
//                    e.printStackTrace();
                    logError(threadName, "?????????????????????????????????Socket");

                    try {
                        socket.close();
                        logInfo(threadName, "Socket????????????");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        logError(threadName, "Socket????????????");
                    }
                }
            }

            cdl.countDown();
            noTakeQueueThreadCdl.countDown();
            logInfo(threadName, "????????????");
        });

//        ????????????
//        ???????????????????????????????????????????????????
        AsyncCaller.run(() -> {
            String threadName = "??????";
            logInfo(threadName, "????????????");
            Random random = new Random();
//            ????????????????????????????????????????????????????????????????????????????????????????????????????????????
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
                    logError(threadName, "?????????????????????????????????Socket");

                    try {
                        socket.close();
                        logInfo(threadName, "Socket????????????");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        logError(threadName, "Socket????????????");
                    }
                }

                if (pingQueue.size() >= 3) {
                    isConnected = false;

                    logError(threadName, "???????????????????????????" + pingQueue.size() + "???????????????????????????Socket");
                    disconnectReason = "???????????????????????????" + pingQueue.size() + "????????????";

                    try {
                        socket.close();
                        logInfo(threadName, "Socket????????????");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        logError(threadName, "Socket????????????");
                    }
                }

//                ??????????????????0???1???ping???
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
            logInfo(threadName, "????????????");
        });

//        ??????????????????
//        ??????????????????????????????????????????
        AsyncCaller.run(() -> {
            String threadName = "????????????";
            logInfo(threadName, "????????????");
//            ?????????????????????????????????????????????
            boolean isFirstTime = true;
            cycle:
            while (isConnected) {
                try {
                    Packet packet = receiveQueue.take();
                    switch (packet.getId().getValue()) {
                        case -1:
//                            ???id???-1??????????????????
                            continue;
                        case 0x20:
//                                ???????????????
                            VarLong ping = new VarLong(packet.getData());
                            Long sent = pingQueue.poll();
                            if (sent == null) {
//                                    ???????????????????????????????????????
                                logError(threadName, "??????????????????????????????????????????????????????Socket");
                                isConnected = false;
                                socket.close();
                                break;
                            }

                            if (!(ping.getValue() == sent + ConfigOperation.getHeartbeat())) {
//                                    ??????????????????????????????
                                logError(threadName, "????????????????????????????????????Socket");
                                isConnected = false;
                                socket.close();
                                break cycle;
                            }

                            if (isFirstTime) {
                                //            ????????????????????????
                                session.sendMessageToAllGroups(new PlainText("???Minecraft????????????????????????\n????????????" + session.getName() + "\n" +
                                        "??????????????????" + serverName.getContent() + "\n" +
                                        "?????????" + serverAddress + "\n" +
                                        "?????????" + new Date()));
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
//                                ???????????????
                            VarIntString exitMsg = new VarIntString(packet.getData());
                            logInfo(threadName, "????????????????????????????????????" + exitMsg.getContent());
                            disconnectReason = "????????????????????????????????????" + exitMsg.getContent();
                            isConnected = false;
                            socket.close();
                            break;
                        case 0x10:
//                                ?????????????????????????????????
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
//                                ????????????????????????????????????
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
//                                ??????????????????????????????????????????
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
//                                ??????????????????????????????
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
//                                ???????????????????????????????????????
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
//                                ????????????????????????????????????
                            Long getOnlineGroup = onlinePlayersCommandSendGroupQueue.poll();
                            if (getOnlineGroup == null) {
//                                    ????????????????????????????????????????????????????????????
                                logError(threadName, "???????????????????????????????????????????????????????????????????????????????????????????????????Socket");
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

//                                ?????????????????????
                            session.sendMessageToGroup(getOnlineGroup, new PlainText(Objects.requireNonNull(res)));
                            break;

                        case 0x22:
//                                ??????????????????
                            VarIntString commandRes = new VarIntString(packet.getData());
                            Long commandGroupId = rconCommandSendGroupQueue.poll();
                            if (commandGroupId == null) {
//                                    ????????????????????????????????????????????????
                                logError(threadName, "???????????????????????????????????????????????????????????????????????????Socket");
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
//                            ????????????????????????
                            VarIntString commandResult = new VarIntString(packet.getData());
                            Long groupId = userCommandGroupQueue.poll();
                            if (groupId == null) {
//                                    ????????????????????????????????????????????????
                                logError(threadName, "??????????????????????????????????????????????????????????????????????????????Socket");
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
//                            ????????????????????????
                            VarIntString msg = new VarIntString(packet.getData());
                            Long id = addUserCommandQueue.poll();

                            if (id == null) {
                                logError(threadName, "??????????????????????????????????????????????????????????????????????????????????????????Socket");
                                isConnected = false;
                                socket.close();
                                break;
                            }

                            session.sendMessageToFriend(id, new PlainText("[????????????] " + msg.getContent()));

                            break;
                        }

                        case 0x26: {
//                            ????????????????????????
                            VarIntString msg = new VarIntString(packet.getData());
                            Long id = delUserCommandQueue.poll();

                            if (id == null) {
                                logError(threadName, "??????????????????????????????????????????????????????????????????????????????????????????Socket");
                                isConnected = false;
                                socket.close();
                                break;
                            }

                            session.sendMessageToFriend(id, new PlainText("[????????????] " + msg.getContent()));

                            break;
                        }

                        case 0x27:
                        case 0x29: {
//                            ????????????????????????
                            Long qq = null;
                            Long groupId = null;
                            if (packet.getId().getValue() == 0x27) {
//                                mcchat??????
                                qq = getMcChatUserCommandsQueue.poll();
                                if (qq == null) {
                                    logError(threadName, "????????????????????????????????????mcchat???????????????????????????????????????????????????Socket");
                                    isConnected = false;
                                    socket.close();
                                    break;
                                }
                            }

                            if (packet.getId().getValue() == 0x29) {
//                                ??????????????????
                                groupId = getUserCommandsGroupQueue.poll();
                                if (groupId == null) {
                                    logError(threadName, "?????????????????????????????????????????????????????????????????????????????????????????????Socket");
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
//                            ??????mcid???qq????????????
                            Long groupId = userBindQueue.poll();
                            if (groupId == null) {
                                logError(threadName, "???????????????????????????????????????????????????????????????????????????????????????Socket");
                                isConnected = false;
                                socket.close();
                                break;
                            }

                            VarIntString msg = new VarIntString(packet.getData());

                            session.sendMessageToGroup(groupId, new PlainText(msg.getContent()));

                            break;
                        }
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                    isConnected = false;
                    logError(threadName, "???????????????????????????Socket");

                    try {
                        socket.close();
                        logInfo(threadName, "Socket????????????");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        logError(threadName, "Socket????????????");
                    }
                }
            }

            cdl.countDown();
            logInfo(threadName, "????????????");
        });

//        ?????? ?????????????????? ??????
        try {
            noTakeQueueThreadCdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        ????????????????????? ??????????????? ??????????????????????????????
        sendQueue.add(new Packet(new VarInt(0), new VarInt(-1), null));
        receiveQueue.add(new Packet(new VarInt(0), new VarInt(-1), null));

//        ????????????????????????
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logInfo("?????????", "??????????????????????????????");

//        ??????socket??????????????????????????????
        while (!socket.isClosed()) {
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        logInfo("Socket", "???????????????");

//        ????????????????????????
        session.delMinecraftThread(this);
        logInfo("?????????", "??????????????????");

//        ????????????????????????
        if (pingRight.size() == 1) {
            session.sendMessageToAllGroups(new PlainText("???Minecraft????????????????????????\n????????????" +
                    session.getName() + "\n??????????????????" +
                    serverName.getContent() + "\n?????????" +
                    serverAddress + "\n?????????" +
                    disconnectReason + "\n?????????" + new Date()));
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
            VarIntString[] getUserCommandsCommands
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

        this.inputStream = new BufferedInputStream(socket.getInputStream());
        this.outputStream = new BufferedOutputStream(socket.getOutputStream());

        this.serverAddress = socket.getRemoteSocketAddress().toString();
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
