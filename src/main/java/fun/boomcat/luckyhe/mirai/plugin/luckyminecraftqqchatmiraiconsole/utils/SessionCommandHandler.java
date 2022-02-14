package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.data.SessionDataOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.SessionDataExistException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.SessionDataGroupExistException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.SessionDataGroupNotExistException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.SessionDataNotExistException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.exception.MinecraftThreadNotFoundException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.Session;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.SessionGroup;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.thread.MinecraftConnectionThread;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.opmcchatcommand.OpMcChatCommandUtil;
import net.mamoe.mirai.console.command.CommandSender;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class SessionCommandHandler {
    public static void selectSession(Object[] args, CommandSender commandSender, String primaryName, String[] secondaryNames) {
        int len = args.length;
        if (len == 0) {
            commandSender.sendMessage(OpMcChatCommandUtil.sessionHelp(primaryName, secondaryNames));
            return;
        }

        String operation = args[0].toString();
        switch (operation) {
            case "list":
                selectList(
                        Arrays.copyOfRange(args, 1, len),
                        commandSender,
                        primaryName,
                        secondaryNames
                );
                break;
            case "add":
                selectAdd(
                        Arrays.copyOfRange(args, 1, len),
                        commandSender,
                        primaryName,
                        secondaryNames
                );
                break;
            case "del":
                selectDel(
                        Arrays.copyOfRange(args, 1, len),
                        commandSender,
                        primaryName,
                        secondaryNames
                );
                break;
            case "modify":
                selectModify(
                        Arrays.copyOfRange(args, 1, len),
                        commandSender,
                        primaryName,
                        secondaryNames
                );
                break;
            case "announce":
                selectAnnounce(
                        Arrays.copyOfRange(args, 1, len),
                        commandSender,
                        primaryName,
                        secondaryNames
                );
                break;
            default:
                commandSender.sendMessage(OpMcChatCommandUtil.sessionHelp(primaryName, secondaryNames));
                return;
        }
    }

    private static void selectList(Object[] args, CommandSender commandSender, String primaryName, String[] secondaryNames) {
        int len = args.length;
        if (len == 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("会话号 / 备注 / 群列表 / 消息格式 / 连接\n");

            List<Session> sessions;
            try {
                sessions = SessionUtil.getSessions();
            } catch (Exception e) {
                e.printStackTrace();
                commandSender.sendMessage("出现其它异常，请联系开发者");
                return;
            }

            for (Session session : sessions) {
                sb.append(session.getId()).append(" / ").append(session.getName()).append(" / ");
                sb.append("[");
                for (int i = 0; i < session.getGroups().size(); i++) {
                    SessionGroup group = session.getGroups().get(i);
                    sb.append(group.getName()).append("(").append(group.getId()).append(")");
                    if (i != session.getGroups().size() - 1) {
                        sb.append(", ");
                    }
                }
                sb.append("]").append(" / ").append(session.getFormatString()).append(" / ");
                sb.append("[");
                for (int i = 0; i < session.getMinecraftThreads().size(); i++) {
                    MinecraftConnectionThread thread = session.getMinecraftThreads().get(i);
                    sb.append(thread.getServerName().getContent()).append("(").append(thread.getServerAddress()).append(")");
                    if (i != session.getMinecraftThreads().size() - 1) {
                        sb.append(", ");
                    }
                }
                sb.append("]").append("\n");
            }

            MessageUtil.pageSender(commandSender, sb.toString());
            return;
        }

        String sessionIdString = args[0].toString();
        long sessionId;
        try {
            sessionId = Long.parseLong(sessionIdString);
        } catch (NumberFormatException e) {
            commandSender.sendMessage("会话号应为数字而不是" + sessionIdString);
            return;
        }

        Session session;
        try {
            session = SessionUtil.getSession(sessionId);
        } catch (SessionDataNotExistException e) {
            commandSender.sendMessage("没有会话号为" + sessionId + "的会话");
            return;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("会话号：").append(session.getId()).append("\n");
        sb.append("会话备注：").append(session.getName()).append("\n");
        sb.append("消息格式：").append(session.getFormatString()).append("\n");
        sb.append("群列表：");

        List<SessionGroup> groups = session.getGroups();
        if (groups.size() == 0) {
            sb.append("空\n");
        } else {
            sb.append("\n");
            for (SessionGroup group : groups) {
                sb.append("    ").append(group.getName()).append("(").append(group.getId()).append(")").append("\n");
            }
        }

        sb.append("连接：");
        List<MinecraftConnectionThread> threads = session.getMinecraftThreads();
        if (threads.size() == 0) {
            sb.append("无\n");
        } else {
            sb.append("\n");
            for (MinecraftConnectionThread thread : threads) {
                sb.append("    ").append(thread.getServerName().getContent()).append("(").append(thread.getServerAddress()).append(")").append("\n");
            }
        }

        commandSender.sendMessage(sb.toString());

    }

    @Deprecated
    @SuppressWarnings("过时的方法，请使用新方法")
    private static void selectListDeprecated(Object[] args, CommandSender commandSender, String primaryName, String[] secondaryNames) {
        int len = args.length;
        if (len == 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("会话号 / 备注 / 群列表 / 消息格式\n");

            List<Object> sessionDataList;
            try {
                sessionDataList = SessionDataOperation.getSessionDataList();
            } catch (Exception e) {
                e.printStackTrace();
                commandSender.sendMessage("出现问题，请联系开发者");
                return;
            }

            for (Object sessionData : sessionDataList) {
                Map<String, Object> map = ((Map<String, Object>) sessionData);
                sb.append(map.get("id")).append(" / ").append(map.get("name")).append(" / ").append(map.get("groups"));
                sb.append(" / ").append(map.get("format")).append("\n");
            }

            MessageUtil.pageSender(commandSender, sb.toString());
            return;
        }

        String sessionIdString = args[0].toString();
        long sessionId;
        try {
            sessionId = Long.parseLong(sessionIdString);
        } catch (NumberFormatException e) {
            commandSender.sendMessage("会话号应为数字而不是" + sessionIdString);
            return;
        }

        Map<String, Object> sessionData;
        try {
            sessionData = SessionDataOperation.getSessionData(sessionId);
        } catch (SessionDataNotExistException e) {
            commandSender.sendMessage("会话" + sessionId + "不存在");
            return;
        } catch (Exception e) {
            e.printStackTrace();
            commandSender.sendMessage("出现其它异常，请联系开发者");
            return;
        }

        commandSender.sendMessage("会话号：" + sessionData.get("id") + "\n会话备注：" + sessionData.get("name") + "\n群号：" + sessionData.get("groups") + "\n消息格式：" + sessionData.get("format"));
    }

    private static void selectAdd(Object[] args, CommandSender commandSender, String primaryName, String[] secondaryNames) {
        int len = args.length;
        if (len == 0) {
            commandSender.sendMessage("请传入会话号参数");
            return;
        }

        String sessionIdString = args[0].toString();
        long sessionId;
        try {
            sessionId = Long.parseLong(sessionIdString);
        } catch (NumberFormatException e) {
            commandSender.sendMessage("会话号为数字，而不是" + sessionIdString);
            return;
        }

        String sessionName;
        try {
            sessionName = args[1].toString();
        } catch (IndexOutOfBoundsException e) {
            commandSender.sendMessage("请传入会话名");
            return;
        }

        Object[] formatStringSlices;
        try {
            formatStringSlices = Arrays.copyOfRange(args, 2, len);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (formatStringSlices.length == 0) {
            commandSender.sendMessage("请传入格式化字符串");
            return;
        }

        StringBuilder formatStringBuilder = new StringBuilder();

        for (Object formatStringSlice : formatStringSlices) {
            formatStringBuilder.append(formatStringSlice).append(" ");
        }

//        剩下的参数全为formatString
        String formatString = formatStringBuilder.toString();
        if (formatString.charAt(formatString.length() - 1) == ' ') {
            formatString = new String(formatString.toCharArray(), 0, formatString.length() - 1);
        }

        try {
            SessionDataOperation.addSessionData(sessionId, sessionName, formatString);
        } catch (SessionDataExistException e) {
            commandSender.sendMessage("会话号" + sessionId + "已存在");
            return;
        } catch (Exception e) {
            e.printStackTrace();
            commandSender.sendMessage("出现其它错误，请联系开发者");
            return;
        }

        commandSender.sendMessage("已添加：\n会话号：" + sessionId + "\n会话名：" + sessionName + "\n消息格式：" + formatString);
    }

    private static void selectDel(Object[] args, CommandSender commandSender, String primaryName, String[] secondaryNames) {
        String sessionIdString;
        try {
            sessionIdString = args[0].toString();
        } catch (IndexOutOfBoundsException e) {
            commandSender.sendMessage("请传入会话号");
            return;
        }

        long sessionId;
        try {
            sessionId = Long.parseLong(sessionIdString);
        } catch (NumberFormatException e) {
            commandSender.sendMessage("会话号是数字而不是" + sessionIdString);
            return;
        }

        try {
            SessionDataOperation.removeSessionData(sessionId);
        } catch (SessionDataNotExistException e) {
            commandSender.sendMessage("会话号" + sessionId + "不存在");
            return;
        } catch (Exception e) {
            e.printStackTrace();
            commandSender.sendMessage("出现其它异常，请联系开发者");
            return;
        }

        commandSender.sendMessage("已删除会话" + sessionId);
    }

    private static void selectModify(Object[] args, CommandSender commandSender, String primaryName, String[] secondaryNames) {
        int len = args.length;
        if (len == 0) {
            commandSender.sendMessage(OpMcChatCommandUtil.modifyHelp(primaryName, secondaryNames));
            return;
        }

        String sessionIdString = args[0].toString();
        long sessionId;
        try {
            sessionId = Long.parseLong(sessionIdString);
        } catch (NumberFormatException e) {
            commandSender.sendMessage("会话号为数字，而不是" + sessionIdString);
            return;
        }

        Map<String, Object> sessionData;
        try {
            sessionData = SessionDataOperation.getSessionData(sessionId);
        } catch (SessionDataNotExistException e) {
            commandSender.sendMessage("没有会话号为" + sessionId + "的会话");
            return;
        } catch (Exception e) {
            e.printStackTrace();
            commandSender.sendMessage("发生了其它异常，请联系开发者");
            return;
        }

        String operation;
        try {
            operation = args[1].toString();
        } catch (IndexOutOfBoundsException e) {
            commandSender.sendMessage("请传入操作参数");
            return;
        }

        switch (operation) {
            case "groupadd":
                selectModifyGroupAdd(
                        Arrays.copyOfRange(args, 2, len),
                        commandSender,
                        sessionId,
                        primaryName,
                        secondaryNames
                );
                break;
            case "groupdel":
                selectModifyGroupDel(
                        Arrays.copyOfRange(args, 2, len),
                        commandSender,
                        sessionId,
                        primaryName,
                        secondaryNames
                );
                break;
            case "name":
                selectModifyName(
                        Arrays.copyOfRange(args, 2, len),
                        commandSender,
                        sessionId,
                        primaryName,
                        secondaryNames
                );
                break;
            case "format":
                selectModifyFormatString(
                        Arrays.copyOfRange(args, 2, len),
                        commandSender,
                        sessionId,
                        primaryName,
                        secondaryNames
                );
                break;
            default:
                commandSender.sendMessage(OpMcChatCommandUtil.modifyHelp(primaryName, secondaryNames));
                break;
        }

    }

    private static void selectModifyGroupAdd(Object[] args, CommandSender commandSender, long sessionId, String primaryName, String[] secondaryNames) {
        int len = args.length;
        if (len == 0) {
            commandSender.sendMessage("请传入新群号");
            return;
        }

        String newGroupIdString = args[0].toString();

        long newGroupId;
        try {
            newGroupId = Long.parseLong(newGroupIdString);
        } catch (NumberFormatException e) {
            commandSender.sendMessage("群号应为数字，而不是" + newGroupIdString);
            return;
        }

        try {
            commandSender.getBot().getGroupOrFail(newGroupId);
        } catch (NoSuchElementException e) {
            commandSender.sendMessage("bot列表中无群" + newGroupId);
            return;
        }

        String groupNickname;
        try {
            groupNickname = args[1].toString();
        } catch (IndexOutOfBoundsException e) {
            commandSender.sendMessage("请传入群昵称参数");
            return;
        }

        try {
            SessionDataOperation.addSessionDataGroup(sessionId, newGroupId, groupNickname);
        } catch (SessionDataNotExistException e) {
            return;
        } catch (SessionDataGroupExistException e) {
            commandSender.sendMessage("会话号" + sessionId + "的群列表中已存在群" + newGroupId);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            commandSender.sendMessage("出现其它异常，请联系开发者");
            return;
        }

        Map<String, Object> sessionData;
        try {
            sessionData = SessionDataOperation.getSessionData(sessionId);
        } catch (SessionDataNotExistException ignored) {
            return;
        } catch (Exception e) {
            e.printStackTrace();
            commandSender.sendMessage("出现其它异常，请联系开发者");
            return;
        }

        commandSender.sendMessage("已将群" + newGroupId + "添加到会话" + sessionId + "中");
        commandSender.sendMessage("会话号：" + sessionData.get("id") + "\n会话备注：" + sessionData.get("name") + "\n群号：" + sessionData.get("groups") + "\n消息格式：" + sessionData.get("format"));
    }

    private static void selectModifyGroupDel(Object[] args, CommandSender commandSender, long sessionId, String primaryName, String[] secondaryNames) {
        int len = args.length;
        if (len == 0) {
            commandSender.sendMessage("请传入群号");
            return;
        }

        String groupIdString = args[0].toString();

        long groupId;
        try {
            groupId = Long.parseLong(groupIdString);
        } catch (NumberFormatException e) {
            commandSender.sendMessage("群号应为数字而不是" + groupIdString);
            return;
        }

        try {
            SessionDataOperation.removeSessionDataGroup(sessionId, groupId);
        } catch (SessionDataNotExistException e) {
            return;
        } catch (SessionDataGroupNotExistException e) {
            commandSender.sendMessage("会话" + sessionId + "的群列表中没有群" + groupId);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            commandSender.sendMessage("出现其它异常，请联系开发者");
            return;
        }

        Map<String, Object> sessionData;
        try {
            sessionData = SessionDataOperation.getSessionData(sessionId);
        } catch (SessionDataNotExistException ignored) {
            return;
        } catch (Exception e) {
            e.printStackTrace();
            commandSender.sendMessage("出现其它异常，请联系开发者");
            return;
        }

        commandSender.sendMessage("已从会话" + sessionId + "删除群" + groupId);
        commandSender.sendMessage("会话号：" + sessionData.get("id") + "\n会话备注：" + sessionData.get("name") + "\n群号：" + sessionData.get("groups") + "\n消息格式：" + sessionData.get("format"));
    }

    private static void selectModifyName(Object[] args, CommandSender commandSender, long sessionId, String primaryName, String[] secondaryNames) {
        int len = args.length;
        if (len == 0) {
            commandSender.sendMessage("请传入新会话备注");
            return;
        }

        String newName = args[0].toString();
        try {
            SessionDataOperation.modifySessionDataName(sessionId, newName);
        } catch (SessionDataNotExistException e) {
            return;
        } catch (Exception e) {
            e.printStackTrace();
            commandSender.sendMessage("发生其它异常，请联系开发者");
            return;
        }

        commandSender.sendMessage("已将会话" + sessionId + "的备注设置为" + newName);

        Map<String, Object> sessionData;
        try {
            sessionData = SessionDataOperation.getSessionData(sessionId);
        } catch (SessionDataNotExistException ignored) {
            return;
        } catch (Exception e) {
            e.printStackTrace();
            commandSender.sendMessage("出现其它异常，请联系开发者");
            return;
        }
        commandSender.sendMessage("会话号：" + sessionData.get("id") + "\n会话备注：" + sessionData.get("name") + "\n群号：" + sessionData.get("groups") + "\n消息格式：" + sessionData.get("format"));
    }

    private static void selectModifyFormatString(Object[] args, CommandSender commandSender, long sessionId, String primaryName, String[] secondaryNames) {
        int len = args.length;
        if (len == 0) {
            commandSender.sendMessage("请传入消息格式参数");
            return;
        }

        StringBuilder formatStringBuilder = new StringBuilder();
        for (Object arg : args) {
            formatStringBuilder.append(arg).append(" ");
        }

        String formatString = formatStringBuilder.toString();
        if (formatString.charAt(formatString.length() - 1) == ' ') {
            formatString = new String(formatString.toCharArray(), 0, formatString.length() - 1);
        }

        try {
            SessionDataOperation.modifySessionDataFormat(sessionId, formatString);
        } catch (SessionDataNotExistException e) {
            return;
        } catch (Exception e) {
            e.printStackTrace();
            commandSender.sendMessage("发生其它异常，请联系开发者");
            return;
        }

        commandSender.sendMessage("已将会话" + sessionId + "的消息格式设置为" + formatString);


        Map<String, Object> sessionData;
        try {
            sessionData = SessionDataOperation.getSessionData(sessionId);
        } catch (SessionDataNotExistException ignored) {
            return;
        } catch (Exception e) {
            e.printStackTrace();
            commandSender.sendMessage("出现其它异常，请联系开发者");
            return;
        }
        commandSender.sendMessage("会话号：" + sessionData.get("id") + "\n会话备注：" + sessionData.get("name") + "\n群号：" + sessionData.get("groups") + "\n消息格式：" + sessionData.get("format"));

    }

    private static void selectAnnounce(Object[] args, CommandSender commandSender, String primaryName, String[] secondaryNames) {
//        处理发送消息相关
        int len = args.length;
        if (len == 0) {
            commandSender.sendMessage(OpMcChatCommandUtil.announceHelp(primaryName, secondaryNames));
            return;
        }

        String sessionIdString = args[0].toString();
        long sessionId;
        try {
            sessionId = Long.parseLong(sessionIdString);
        } catch (NumberFormatException e) {
            commandSender.sendMessage("会话号为整数而不是" + sessionIdString);
            return;
        }

        Session session;
        try {
            session = SessionUtil.getSession(sessionId);
        } catch (SessionDataNotExistException e) {
            commandSender.sendMessage("没有会话号为" + sessionId + "的会话");
            return;
        } catch (Exception e) {
            e.printStackTrace();
            commandSender.sendMessage("出现异常，请稍后重试");
            return;
        }

        String operation;
        try {
            operation = args[1].toString();
        } catch (IndexOutOfBoundsException e) {
            commandSender.sendMessage("请传入操作");
            return;
        }

        switch (operation) {
            case "mc":
                selectAnnounceMc(
                        Arrays.copyOfRange(args, 2, len),
                        commandSender,
                        session,
                        primaryName,
                        secondaryNames
                );
                break;
            case "mcall":
                selectAnnounceMcAll(
                        Arrays.copyOfRange(args, 2, len),
                        commandSender,
                        session,
                        primaryName,
                        secondaryNames
                );
                break;
            default:
                commandSender.sendMessage(OpMcChatCommandUtil.announceHelp(primaryName, secondaryNames));
                return;
        }
    }

    private static void selectAnnounceMc(Object[] args, CommandSender commandSender, Session session, String primaryName, String[] secondaryNames) {
        int len = args.length;
        if (len == 0) {
            commandSender.sendMessage("请传入MC端服务器名");
            return;
        }

        String serverName = args[0].toString();

        try {
            args[1].toString();
        } catch (IndexOutOfBoundsException e) {
            commandSender.sendMessage("请传入公告内容");
            return;
        }

        StringBuilder announcement = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            announcement.append(args[i].toString());
            if (i == args.length - 1) {
                announcement.append(" ");
            }
        }

        try {
            session.sendAnnouncementToMinecraftConnection(
                    commandSender.getUser().getId(),
                    commandSender.getUser().getNick(),
                    serverName,
                    announcement.toString()
            );
        } catch (MinecraftThreadNotFoundException e) {
            commandSender.sendMessage("会话" + session.getId() + "没有名为" + serverName + "的连接");
        }

        commandSender.sendMessage("已向会话" + session.getId() + "的" + serverName + "连接发送公告：\n" + announcement);
    }

    private static void selectAnnounceMcAll(Object[] args, CommandSender commandSender, Session session, String primaryName, String[] secondaryNames) {
        int len = args.length;
        if (len == 0) {
            commandSender.sendMessage("请传入公告内容");
            return;
        }

        StringBuilder announcement = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            announcement.append(args[i].toString());
            if (i == args.length - 1) {
                announcement.append(" ");
            }
        }

        session.sendAnnouncementToAllMinecraftConnections(
                commandSender.getUser().getId(),
                commandSender.getUser().getNick(),
                announcement.toString()
        );
    }
}
