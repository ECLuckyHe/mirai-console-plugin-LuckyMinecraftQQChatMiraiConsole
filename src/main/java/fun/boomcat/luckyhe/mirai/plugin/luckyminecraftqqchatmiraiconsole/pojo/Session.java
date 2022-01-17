package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.config.ConfigOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.datatype.VarInt;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.datatype.VarIntString;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.datatype.VarLong;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.pojo.Packet;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.util.ByteUtil;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.thread.MinecraftConnectionThread;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.QqFormatPlaceholder;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.ReplacePlaceholderUtil;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

public class Session {
    private final long id;
    private final String name;
    private final List<SessionGroup> groups;
    private final String formatString;
    private final List<MinecraftConnectionThread> minecraftThreads = new ArrayList<>();

    public boolean hasGroup(long groupId) {
        for (SessionGroup group : groups) {
            if (group.getId() == groupId) {
                return true;
            }
        }
        return false;
    }

    public void sendClosePacketToMinecraftThread(String info) {
        for (MinecraftConnectionThread thread : minecraftThreads) {
            thread.sendClosePacket(info);
        }
    }

    public void addMinecraftThread(MinecraftConnectionThread newMinecraftConnectionThread) {
        synchronized (minecraftThreads) {
            minecraftThreads.add(newMinecraftConnectionThread);
        }
    }

    public void delMinecraftThread(MinecraftConnectionThread minecraftConnectionThread) {
        synchronized (minecraftThreads) {
            minecraftThreads.remove(minecraftConnectionThread);
        }
    }

    public void sendMessageFromMinecraftThread(MinecraftConnectionThread thread, String content) {
//        来自服务器的消息发送到群内
        sendSessionGroupsFromMinecraftThread(content);
        sendSessionMinecraftThreadsFromMinecraftThread(thread, content);
    }

    public void sendSessionGroupsFromMinecraftThread(String content) {
        for (SessionGroup group : groups) {
            try {
//                此处：只支持登录一个bot
                Bot.getInstances().get(0).getGroupOrFail(group.getId()).sendMessage(content);
            } catch (NoSuchElementException ignored) {

            }
        }
    }

    public void sendSessionMinecraftThreadsFromMinecraftThread(MinecraftConnectionThread thread, String content) {
//        游戏向游戏发送内容
        VarInt packetId = new VarInt(0x11);
        VarIntString string = new VarIntString(content);
        for (MinecraftConnectionThread t : minecraftThreads) {
            if (t != thread) {
                t.addSendQueue(new Packet(
                        new VarInt(packetId.getBytesLength() + string.getBytesLength()),
                        packetId,
                        string.getBytes()
                ));
            }
        }
    }

    public void sendMessageFromGroup(
            Bot bot,
            long groupId,
            String groupName,
            long senderId,
            String senderNickname,
            String senderGroupNickname,
            MessageChain message
    ) throws FileNotFoundException {
//        处理从群来的消息

//        如果消息是该内容的时候发送获取在线玩家信息数据
        if (ConfigOperation.getOnlinePlayersCommand().contains(message.contentToString())) {
            VarInt onlinePlayersPacketId = new VarInt(0x21);
            VarLong groupIdLong = new VarLong(groupId);
            sendPacket(new Packet(
                    new VarInt(onlinePlayersPacketId.getBytesLength() + groupIdLong.getBytesLength()),
                    onlinePlayersPacketId,
                    groupIdLong.getBytes()
            ));
        }

        sendSessionGroupsFromGroup(bot, groupId, groupName, senderId, senderNickname, senderGroupNickname, message);
        sendSessionMinecraftThreadsFromGroup(groupId, groupName, senderId, senderNickname, senderGroupNickname, message.contentToString());
    }

    private void sendSessionMinecraftThreadsFromGroup(
            long groupId,
            String groupName,
            long senderId,
            String senderNickname,
            String senderGroupNickname,
            String message
    ) {
//        群->mc
//        转成对应类型
        String groupNickname = getGroupNickname(groupId);
        if (groupNickname == null) {
            return;
        }

        VarInt packetId = new VarInt(0x10);
        VarLong gi = new VarLong(groupId);
        VarIntString gn = new VarIntString(groupName);
        VarIntString gnm = new VarIntString(groupNickname);
        VarLong si = new VarLong(senderId);
        VarIntString snm = new VarIntString(senderNickname);
        VarIntString sgnm = new VarIntString(senderGroupNickname.length() == 0 ? senderNickname : senderGroupNickname);
        VarIntString msg = new VarIntString(message);

        synchronized (minecraftThreads) {
            for (MinecraftConnectionThread thread : minecraftThreads) {
//                添加进发送队列中
                thread.addSendQueue(new Packet(
                        new VarInt(
                                packetId.getBytesLength() + gi.getBytesLength() +
                                        gn.getBytesLength() + gnm.getBytesLength() +
                                        si.getBytesLength() + snm.getBytesLength() +
                                        sgnm.getBytesLength() + msg.getBytesLength()
                        ),
                        packetId,
                        ByteUtil.byteMergeAll(
                                gi.getBytes(),
                                gn.getBytes(),
                                gnm.getBytes(),
                                si.getBytes(),
                                snm.getBytes(),
                                sgnm.getBytes(),
                                msg.getBytes()
                        )
                ));
            }
        }
    }

    private void sendSessionGroupsFromGroup(
            Bot bot,
            long groupId,
            String groupName,
            long senderId,
            String senderNickname,
            String senderGroupNickname,
            MessageChain message
    ) {
//        群->群
        String groupNickname = getGroupNickname(groupId);
        if (groupNickname == null) {
            return;
        }

        Random random = new Random();
//        此处思路：先将%message%替换为%message%xxxx，其中xxxx是数字
//        在后面再替换%message%xxxx为实际消息内容
        String messagePlaceholder = QqFormatPlaceholder.MESSAGE + random.nextLong();

        String res = ReplacePlaceholderUtil.replacePlaceholderWithString(
                formatString,
                QqFormatPlaceholder.SESSION_NAME,
                name,
                QqFormatPlaceholder.GROUP_ID,
                String.valueOf(groupId),
                QqFormatPlaceholder.GROUP_NAME,
                groupName,
                QqFormatPlaceholder.GROUP_NICKNAME,
                groupNickname,
                QqFormatPlaceholder.SENDER_ID,
                String.valueOf(senderId),
                QqFormatPlaceholder.SENDER_NICKNAME,
                senderNickname,
                QqFormatPlaceholder.SENDER_GROUP_NICKNAME,
                senderGroupNickname.equals("") ? senderNickname : senderGroupNickname,
                QqFormatPlaceholder.MESSAGE,
                messagePlaceholder
        );

        String[] splitTempRes = res.split(messagePlaceholder, -1);
        MessageChainBuilder mcb = new MessageChainBuilder();
        if (splitTempRes.length <= 1) {
            mcb.append(res);
        } else {
            for (int i = 0; i < splitTempRes.length; i++) {
                mcb.append(splitTempRes[i]);
                if (!(i == splitTempRes.length - 1)) {
                    mcb.append(message);
                }
            }
        }

        for (SessionGroup group : groups) {
            if (group.getId() != groupId) {
                try {
                    bot.getGroupOrFail(group.getId()).sendMessage(mcb.build());
                } catch (NoSuchElementException ignored) {

                }
            }
        }
    }

    private void sendPacket(Packet packet) {
        for (MinecraftConnectionThread thread : minecraftThreads) {
            thread.addSendQueue(packet);
        }
    }

    private String getGroupNickname(long groupId) {
        for (SessionGroup group : groups) {
            if (group.getId() == groupId) {
                return group.getName();
            }
        }
        return null;
    }

    public void sendMessageToGroup(long groupId, String message) {
        try {
            Bot.getInstances().get(0).getGroupOrFail(groupId).sendMessage(message);
        } catch (Exception e) {

        }
    }

    public void sendMessageToAllGroups(String message) {
        for (SessionGroup group : groups) {
            try {
                Bot.getInstances().get(0).getGroupOrFail(group.getId()).sendMessage(message);
            } catch (Exception e) {

            }
        }
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<SessionGroup> getGroups() {
        return groups;
    }

    public String getFormatString() {
        return formatString;
    }

    public List<MinecraftConnectionThread> getMinecraftThreads() {
        return minecraftThreads;
    }

    public Session(long id, String name, List<SessionGroup> groups, String formatString) {
        this.id = id;
        this.name = name;
        this.groups = groups;
        this.formatString = formatString;
    }

    @Override
    public String toString() {
        return "Session{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", groups=" + groups +
                ", formatString='" + formatString + '\'' +
                '}';
    }
}
