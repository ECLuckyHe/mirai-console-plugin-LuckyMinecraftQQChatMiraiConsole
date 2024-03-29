package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.listener;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.LuckyMinecraftQQChatMiraiConsole;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.data.SessionDataOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.*;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.exception.MinecraftThreadNotFoundException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.Session;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.SessionGroup;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.thread.MinecraftConnectionThread;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.MessageUtil;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.SessionModifyUtil;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.SessionUtil;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.mcchatcommand.McChatCommandStep;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.mcchatcommand.McChatCommandStepUtil;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.pojo.Announcement;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.pojo.UserCommand;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.pojo.UserCommandAdd;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.pojo.UserCommandDel;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListenerHost;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class McCommandStepListener implements ListenerHost {
    private LuckyMinecraftQQChatMiraiConsole INSTANCE;
    //    临时存放正在进行操作的会话
    private final static Map<Long, Session> modifySessionIdTempMap = new HashMap<>();
    //    临时存放正在发送的公告对象
    private final static Map<Long, Announcement> announceTempMap = new HashMap<>();
    //    临时存放修改用户指令的对象
    private final static Map<Long, UserCommand> userCommandTempMap = new HashMap<>();
    //    临时存放修改用户指令的添加指令对象
    private final static Map<Long, UserCommandAdd> userCommandAddTempMap = new HashMap<>();
//    临时存放修改用户指令的删除指令对象
    private final static Map<Long, UserCommandDel> userCommandDelTempMap = new HashMap<>();

    public McCommandStepListener(LuckyMinecraftQQChatMiraiConsole I) {
        this.INSTANCE = I;
    }

    @EventHandler
    public void onFriendMessage(FriendMessageEvent e) {
        String commandPrefix = CommandManager.INSTANCE.getCommandPrefix();
        MessageChain message = e.getMessage();
        String content = message.contentToString();
        Contact subject = e.getSubject();
        User sender = e.getSender();

        if ((commandPrefix + INSTANCE.getMcChatCommandPrimaryName()).equalsIgnoreCase(content)) {
            return;
        }

        for (String secondaryName : INSTANCE.getMcChatCommandSecondaryNames()) {
            if ((commandPrefix + secondaryName).equalsIgnoreCase(content)) {
                return;
            }
        }

        McChatCommandStep step = McChatCommandStepUtil.getStep(sender.getId());
        if (step == null) {
            return;
        }

        if ("quit".equalsIgnoreCase(content)) {
            McChatCommandStepUtil.clearStep(sender.getId(), subject, "已退出指令");
            return;
        }

        switch (step) {
            case MAIN:
                onMainMenu(step, subject, sender, content);
                break;
            case LIST:
                onList(step, subject, sender, content);
                break;
            case MODIFY:
                onModify(step, subject, sender, content);
                break;
            case MODIFY_MAIN:
                onModifyMain(step, subject, sender, content);
                break;
            case MODIFY_ADD_GROUP:
                onModifyAddGroup(step, subject, sender, content);
                break;
            case MODIFY_DEL_GROUP:
                onModifyDelGroup(step, subject, sender, content);
                break;
            case MODIFY_FORMAT:
                onModifyFormat(step, subject, sender, content);
                break;
            case MODIFY_SESSION_NAME:
                onModifySessionName(step, subject, sender, content);
                break;
            case ANNOUNCE:
                onAnnounce(step, subject, sender, content);
                break;
            case ANNOUNCE_CONTENT:
                onAnnounceContent(step, subject, sender, content);
                break;
            case ANNOUNCE_MC:
                onAnnounceMc(step, subject, sender, content);
                break;
            case USER_COMMAND:
                onUserCommand(step, subject, sender, content);
                break;
            case USER_COMMAND_MENU:
                onUserCommandMenu(step, subject, sender, content);
                break;
            case USER_COMMAND_ADD_NAME:
                onUserCommandAddName(step, subject, sender, content);
                break;
            case USER_COMMAND_ADD_COMMAND:
                onUserCommandAddCommand(step, subject, sender, content);
                break;
            case USER_COMMAND_ADD_MAPPING:
                onUserCommandAddMapping(step, subject, sender, content);
                break;
            case USER_COMMAND_ADD_CONFIRM:
                onUserCommandAddConfirm(step, subject, sender, content);
                break;
            case USER_COMMAND_DEL:
                onUserCommandDelCommand(step, subject, sender, content);
                break;
        }
    }

    public void onMainMenu(McChatCommandStep step, Contact subject, User sender, String content) {
        switch (content.toLowerCase()) {
            case "list":
//                查看名下会话信息
                McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.LIST, subject);
                break;
            case "modify":
//                修改名下会话信息
                McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.MODIFY, subject);
                break;
            case "announce":
                Announcement announcement = new Announcement();
                try {
                    subject.sendMessage(announcement.toOutputString(SessionUtil.getUserSessions(sender.getId())));
                } catch (Exception e) {
                    e.printStackTrace();
                    subject.sendMessage("在获取会话列表时产生异常，请稍后重试或联系开发者");
                    subject.sendMessage(step.getInstruction());
                    return;
                }
                announceTempMap.put(sender.getId(), announcement);
                McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.ANNOUNCE, subject);
                break;
//                用户指令相关
            case "uc":
                McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.USER_COMMAND, subject);
                break;
            case "exit":
                McChatCommandStepUtil.clearStep(sender.getId(), subject, "已退出指令");
                break;
            default:
                subject.sendMessage("无该指令");
                subject.sendMessage(step.getInstruction());
                break;
        }
    }

    public void onList(McChatCommandStep step, Contact subject, User sender, String content) {
        switch (content.toLowerCase()) {
            case "all":
                StringBuilder userSessionInfo = new StringBuilder();
                userSessionInfo.append("主菜单/查看名下会话信息/所有会话号\n");
                userSessionInfo.append("====================\n");
                userSessionInfo.append("会话号(会话名)\n");
                List<Session> sessions;
                try {
                    sessions = SessionUtil.getUserSessions(sender.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                    subject.sendMessage("出现其它异常，请稍后重试或联系开发者");
                    subject.sendMessage(step.getInstruction());
                    return;
                }

                for (Session session : sessions) {
                    userSessionInfo.append(session.getId()).append("(").append(session.getName()).append(")").append("\n");
                }

                MessageUtil.pageSender(subject, userSessionInfo.toString());
                subject.sendMessage(step.getInstruction());
                return;
            case "exit":
                McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.MAIN, subject);
                return;
        }

//        输入会话号
        long sessionId;
        try {
            sessionId = Long.parseLong(content);
        } catch (NumberFormatException e) {
            subject.sendMessage("会话号应为数字，而不是" + content);
            subject.sendMessage(step.getInstruction());
            return;
        }

        Session session;
        try {
            session = SessionUtil.getUserSession(sessionId, sender.getId());
        } catch (SessionDataNotExistException e) {
            subject.sendMessage("会话号为" + sessionId + "的会话不存在");
            subject.sendMessage(step.getInstruction());
            return;
        } catch (Exception e) {
            e.printStackTrace();
            subject.sendMessage("出现其它异常，请稍后重试或联系开发者");
            subject.sendMessage(step.getInstruction());
            return;
        }

        String sb = "主菜单/查看名下会话信息/会话" + sessionId + "\n" +
                "====================\n" +
                SessionUtil.sessionToString(session);

        subject.sendMessage(sb);
        subject.sendMessage(step.getInstruction());
    }

    public void onModify(McChatCommandStep step, Contact subject, User sender, String content) {
//        修改会话输入会话号
        if ("exit".equalsIgnoreCase(content)) {
            McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.MAIN, subject);
            return;
        }

        if ("list".equalsIgnoreCase(content)) {
            //                查看所有会话号
            StringBuilder allSessionInfo = new StringBuilder();
            allSessionInfo.append("主菜单/查看名下会话信息/所有会话号\n");
            allSessionInfo.append("====================\n");
            allSessionInfo.append("会话号(会话名)\n");
            List<Session> sessions;
            try {
                sessions = SessionUtil.getUserSessions(sender.getId());
            } catch (Exception e) {
                e.printStackTrace();
                subject.sendMessage("出现其它异常，请稍后重试或联系开发者");
                subject.sendMessage(step.getInstruction());
                return;
            }

            for (Session session : sessions) {
                allSessionInfo.append(session.getId()).append("(").append(session.getName()).append(")").append("\n");
            }

            MessageUtil.pageSender(subject, allSessionInfo.toString());
            subject.sendMessage(step.getInstruction());
            return;
        }

        long modifySessionId;
        try {
            modifySessionId = Long.parseLong(content);
        } catch (NumberFormatException e) {
            subject.sendMessage("会话号应为数字而不是" + content);
            subject.sendMessage(step.getInstruction());
            return;
        }

        Session session;
        try {
            session = SessionUtil.getUserSession(modifySessionId, sender.getId());
        } catch (SessionDataNotExistException e) {
            subject.sendMessage("没有会话号为" + modifySessionId + "的会话");
            subject.sendMessage(step.getInstruction());
            return;
        } catch (Exception e) {
            e.printStackTrace();
            subject.sendMessage("出现其它异常，请稍后重试或联系开发者");
            subject.sendMessage(step.getInstruction());
            return;
        }

        Session copiedSession = SessionUtil.copySessionWithNoThreads(session);
        modifySessionIdTempMap.put(sender.getId(), copiedSession);
        subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(copiedSession));
        McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.MODIFY_MAIN, subject);
    }

    public void onModifyMain(McChatCommandStep step, Contact subject, User sender, String content) {
        Session tempSession = modifySessionIdTempMap.get(sender.getId());

        switch (content.toLowerCase()) {
            case "exit":
                McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.MAIN, subject);
                break;
            case "gadd":
                subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
                McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.MODIFY_ADD_GROUP, subject);
                break;
            case "gdel":
                subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
                McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.MODIFY_DEL_GROUP, subject);
                break;
            case "format":
                subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
                McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.MODIFY_FORMAT, subject);
                break;
            case "name":
                subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
                McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.MODIFY_SESSION_NAME, subject);
                break;
            case "ok":
                onModifyOk(step, subject, sender, content);
                break;
            default:
                subject.sendMessage("无该指令");
                subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
                subject.sendMessage(step.getInstruction());
                break;
        }
    }

    public void onModifyAddGroup(McChatCommandStep step, Contact subject, User sender, String content) {
//        添加互通群
        Session tempSession = modifySessionIdTempMap.get(sender.getId());

        if ("exit".equalsIgnoreCase(content)) {
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.MODIFY_MAIN, subject);
            return;
        }

        String[] split = content.split(" ");
        if (split.length < 2) {
            subject.sendMessage("不符合要求的格式");
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            subject.sendMessage(step.getInstruction());
            return;
        }

        String newGroupIdString = split[0];
        long newGroupId;
        try {
            newGroupId = Long.parseLong(newGroupIdString);
        } catch (NumberFormatException e) {
            subject.sendMessage("群号应该为数字而不是" + newGroupIdString);
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            subject.sendMessage(step.getInstruction());
            return;
        }

        String newGroupNickname = content.substring(newGroupIdString.length()).trim();
        if (newGroupNickname.equalsIgnoreCase("")) {
            subject.sendMessage("群名不应为空");
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            subject.sendMessage(step.getInstruction());
            return;
        }

        for (SessionGroup group : tempSession.getGroups()) {
            if (group.getId() == newGroupId) {
                subject.sendMessage("群" + group.getId() + "已存在");
                subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
                subject.sendMessage(step.getInstruction());
                return;
            }
        }

        tempSession.getGroups().add(new SessionGroup(newGroupId, newGroupNickname));
        subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
        subject.sendMessage(step.getInstruction());
    }

    public void onModifyDelGroup(McChatCommandStep step, Contact subject, User sender, String content) {
//        删除互通群
        Session tempSession = modifySessionIdTempMap.get(sender.getId());

        if ("exit".equalsIgnoreCase(content)) {
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.MODIFY_MAIN, subject);
            return;
        }

        long delGroupId;
        try {
            delGroupId = Long.parseLong(content);
        } catch (NumberFormatException e) {
            subject.sendMessage("群号应为数字而不是" + content);
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            subject.sendMessage(step.getInstruction());
            return;
        }

        if (!tempSession.hasGroup(delGroupId)) {
            subject.sendMessage("群" + delGroupId + "不存在于会话中");
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            subject.sendMessage(step.getInstruction());
            return;
        }

        tempSession.getGroups().removeIf(s -> s.getId() == delGroupId);
        subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
        subject.sendMessage(step.getInstruction());
    }

    public void onModifyFormat(McChatCommandStep step, Contact subject, User sender, String content) {
//        修改互通格式
        Session tempSession = modifySessionIdTempMap.get(sender.getId());

        String newFormat = null;
        switch (content.toLowerCase()) {
            case "exit":
                subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(modifySessionIdTempMap.get(sender.getId())));
                McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.MODIFY_MAIN, subject);
                return;
            case "default":
                newFormat = "[%groupNickname%] <%senderGroupNickname%> %message%";
                break;
        }

        if (newFormat == null) {
            newFormat = content;
        }

        tempSession.setFormatString(newFormat);
        subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
        McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.MODIFY_MAIN, subject);

    }

    public void onModifySessionName(McChatCommandStep step, Contact subject, User sender, String content) {
//        修改会话名
        Session tempSession = modifySessionIdTempMap.get(sender.getId());

        if ("exit".equalsIgnoreCase(content)) {
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.MODIFY_MAIN, subject);
            return;
        }

        tempSession.setName(content);
        subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
        McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.MODIFY_MAIN, subject);

    }

    public void onModifyOk(McChatCommandStep step, Contact subject, User sender, String content) {
        Session tempSession = modifySessionIdTempMap.get(sender.getId());
        Session oldSession;

//        获取原本的会话
        try {
            oldSession = SessionUtil.getSession(tempSession.getId());
        } catch (SessionDataNotExistException e) {
            subject.sendMessage("会话号为" + tempSession.getId() + "的会话不存在");
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            subject.sendMessage(step.getInstruction());
            return;
        } catch (Exception e) {
            e.printStackTrace();
            subject.sendMessage("出现其它异常，请稍后重试或联系开发者");
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            subject.sendMessage(step.getInstruction());
            return;
        }

        if (SessionModifyUtil.isSessionModifying) {
            subject.sendMessage("当前正有其它重要操作正在执行，执行完毕后开始应用会话设置");
        }

        synchronized (SessionModifyUtil.sessionModifyLock) {
            SessionModifyUtil.isSessionModifying = true;
            subject.sendMessage("正在应用会话设置，请稍等");

//        对会话名进行修改
            if (!tempSession.getName().equals(oldSession.getName())) {
                try {
                    SessionDataOperation.modifySessionDataName(tempSession.getId(), tempSession.getName());
                    subject.sendMessage("原会话名：" + oldSession.getName() + "\n" +
                            "修改为：" + tempSession.getName());
                } catch (SessionDataNotExistException e) {
                    subject.sendMessage("修改会话名时发现会话号为" + tempSession.getId() + "的会话不存在");
                } catch (Exception e) {
                    e.printStackTrace();
                    subject.sendMessage("修改会话名时出现其它异常，请稍后重试或联系开发者");
                }
            }

//        对消息格式进行修改
            if (!tempSession.getFormatString().equals(oldSession.getFormatString())) {
                try {
                    SessionDataOperation.modifySessionDataFormat(tempSession.getId(), tempSession.getFormatString());
                    subject.sendMessage("原消息格式：" + oldSession.getFormatString() + "\n" +
                            "修改为：" + tempSession.getFormatString());
                } catch (SessionDataNotExistException e) {
                    subject.sendMessage("修改消息格式时发现会话号为" + tempSession.getId() + "的会话不存在");
                } catch (Exception e) {
                    e.printStackTrace();
                    subject.sendMessage("修改消息格式时出现其它异常，请稍后重试或联系开发者");
                }
            }

//        对不在列表的群进行添加
            for (SessionGroup group : tempSession.getGroups()) {
                if (!oldSession.hasGroup(group.getId())) {
                    try {
                        SessionDataOperation.addSessionDataGroup(tempSession.getId(), group.getId(), group.getName());
                        subject.sendMessage("添加互通群：" + group.getId() + "(" + group.getName() + ")");
                    } catch (SessionDataGroupExistException e) {
                        subject.sendMessage("添加互通群时发现群" + group.getId() + "已存在");
                    } catch (SessionDataNotExistException e) {
                        subject.sendMessage("添加互通群时发现会话号为" + tempSession.getId() + "的会话不存在");
                    } catch (Exception e) {
                        e.printStackTrace();
                        subject.sendMessage("添加互通群时出现其它异常，请稍后重试或联系开发者");
                    }
                } else {
//                删除掉在oldSession和tempSession中都存在的群
                    oldSession.getGroups().removeIf(s -> s.getId() == group.getId());
                }
            }

//        此处之后剩下的为要删除的群号
            for (SessionGroup group : oldSession.getGroups()) {
                try {
                    SessionDataOperation.removeSessionDataGroup(tempSession.getId(), group.getId());
                    subject.sendMessage("删除互通群：" + group.getId() + "(" + group.getName() + ")");
                } catch (SessionDataGroupNotExistException e) {
                    subject.sendMessage("删除互通群时发现群" + group.getId() + "不存在");
                } catch (SessionDataNotExistException e) {
                    subject.sendMessage("删除互通群时发现会话号为" + tempSession.getId() + "的会话不存在");
                } catch (Exception e) {
                    e.printStackTrace();
                    subject.sendMessage("删除互通群时出现其它异常，请稍后重试或联系开发者");
                }
            }

//        对不在列表的管理员进行添加
            for (Long administrator : tempSession.getAdministrators()) {
                if (!oldSession.hasAdministrator(administrator)) {
                    try {
                        SessionDataOperation.addSessionDataAdministrator(tempSession.getId(), administrator);
                        subject.sendMessage("添加管理员：" + administrator);
                    } catch (SessionDataAdministratorExistException e) {
                        subject.sendMessage("添加互通群时发现管理员" + administrator + "已存在");
                    } catch (SessionDataNotExistException e) {
                        subject.sendMessage("添加管理员QQ时发现会话号为" + tempSession.getId() + "的会话不存在");
                    } catch (Exception e) {
                        e.printStackTrace();
                        subject.sendMessage("添加管理员QQ时出现其它异常，请稍后重试或联系开发者");
                    }
                } else {
                    oldSession.getAdministrators().removeIf(o -> Objects.equals(o, administrator));
                }
            }

//        此处之后剩下的为要删除的管理员
            for (Long administrator : oldSession.getAdministrators()) {
                try {
                    SessionDataOperation.removeSessionDataAdministrator(tempSession.getId(), administrator);
                    subject.sendMessage("删除管理员：" + administrator);
                } catch (SessionDataNotExistException e) {
                    subject.sendMessage("删除管理员时发现会话号为" + tempSession.getId() + "的会话不存在");
                } catch (SessionDataAdministratorNotExistException e) {
                    subject.sendMessage("删除管理员时发现管理员" + administrator + "不存在");
                } catch (Exception e) {
                    e.printStackTrace();
                    subject.sendMessage("删除管理员时出现其它异常，请稍后重试或联系开发者");
                }
            }

            SessionModifyUtil.isSessionModifying = false;
        }

//        获取修改完成后的会话
        Session modifiedSession;
        try {
            modifiedSession = SessionUtil.getSession(tempSession.getId());
        } catch (SessionDataNotExistException e) {
            subject.sendMessage("在获取修改后会话时发现没有会话号为" + tempSession.getId() + "的会话");
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            subject.sendMessage(step.getInstruction());
            return;
        } catch (Exception e) {
            e.printStackTrace();
            subject.sendMessage("在获取修改后会话时出现其它异常，请稍后重试或联系开发者");
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            subject.sendMessage(step.getInstruction());
            return;
        }

        subject.sendMessage("对于会话" + tempSession.getId() + "的修改已完成\n" +
                "====================\n" + SessionUtil.sessionToString(modifiedSession));
        McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.MAIN, subject);
    }

    public void onAnnounce(McChatCommandStep step, Contact subject, User sender, String content) {
//        发送公告

        if ("exit".equalsIgnoreCase(content)) {
            McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.MAIN, subject);
            return;
        }

        List<Session> sessions;
        try {
            sessions = SessionUtil.getUserSessions(sender.getId());
        } catch (Exception e) {
            e.printStackTrace();
            subject.sendMessage("发送公告功能在获取会话列表时产生异常，请稍后重试或联系开发者");
            subject.sendMessage(step.getInstruction());
            return;
        }

        Announcement announcement = announceTempMap.get(sender.getId());

        switch (content.toLowerCase()) {
            case "content":
                subject.sendMessage(announcement.toOutputString(sessions));
                McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.ANNOUNCE_CONTENT, subject);
                break;
            case "mc":
                subject.sendMessage(announcement.toOutputString(sessions));
                McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.ANNOUNCE_MC, subject);
                break;
            case "ok":
                onAnnounceOk(step, subject, sender, content);
                break;
            default:
                subject.sendMessage("没有该指令");
                subject.sendMessage(announcement.toOutputString(sessions));
                subject.sendMessage(step.getInstruction());
                break;
        }
    }

    public void onAnnounceMc(McChatCommandStep step, Contact subject, User sender, String content) {
//        选定发送公告连接
        List<Session> sessions;
        try {
            sessions = SessionUtil.getUserSessions(sender.getId());
        } catch (Exception e) {
            e.printStackTrace();
            subject.sendMessage("发送公告连接设置在获取会话列表时产生异常，请稍后重试或联系开发者");
            McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.ANNOUNCE, subject);
            return;
        }

        Announcement announcement = announceTempMap.get(sender.getId());

        if ("exit".equalsIgnoreCase(content)) {
            subject.sendMessage(announcement.toOutputString(sessions));
            McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.ANNOUNCE, subject);
            return;
        }

        if ("all".equalsIgnoreCase(content)) {
//            判断是否已经全选
            boolean isAll = true;
            for (Session session : sessions) {
//                此处不需要对threadNames赋值的原因是在前面的toOutputString中已经初始化过了
                List<String> threadNames = announcement.getThreadsMap().get(session);
                for (MinecraftConnectionThread thread : session.getMinecraftThreads()) {
                    if (!threadNames.contains(thread.getServerName().getContent())) {
                        isAll = false;
                        break;
                    }
                }
            }

            if (isAll) {
//                未全选则全选，否则反选
                announcement.clearAllMinecraftThreads(sessions);
            } else {
                announcement.selectAllMinecraftThreadsOfAllSession(sessions);
            }

            subject.sendMessage(announcement.toOutputString(sessions));
            subject.sendMessage(step.getInstruction());
            return;
        }

//        以下为指定会话和连接名
        String[] split = content.trim().split(" ");

        String sessionIdString = split[0];
        long sessionId;
        try {
            sessionId = Long.parseLong(sessionIdString);
        } catch (NumberFormatException e) {
            subject.sendMessage("会话号应为数字而不是" + sessionIdString);
            subject.sendMessage(announcement.toOutputString(sessions));
            subject.sendMessage(step.getInstruction());
            return;
        }

        Session session;
        try {
            session = SessionUtil.getUserSession(sessionId, sender.getId());
        } catch (SessionDataNotExistException e) {
            subject.sendMessage("会话号为" + sessionId + "的会话不存在");
            subject.sendMessage(announcement.toOutputString(sessions));
            subject.sendMessage(step.getInstruction());
            return;
        } catch (Exception e) {
            e.printStackTrace();
            subject.sendMessage("获取会话时出现其它异常，请稍后重试或联系开发者");
            subject.sendMessage(announcement.toOutputString(sessions));
            subject.sendMessage(step.getInstruction());
            return;
        }

        if (split.length == 1) {
//            判断是否为全选，如未全选则全选，否则反选
            List<MinecraftConnectionThread> minecraftThreads = session.getMinecraftThreads();

            boolean isAllIn = true;
            for (MinecraftConnectionThread thread : minecraftThreads) {
                if (!announcement.getThreadsMap().get(session).contains(thread.getServerName().getContent())) {
                    isAllIn = false;
                    break;
                }
            }

            if (isAllIn) {
                announcement.clearMinecraftThreads(session);
            } else {
                announcement.selectAllMinecraftThreadsOfOneSession(session);
            }

            subject.sendMessage(announcement.toOutputString(sessions));
            subject.sendMessage(step.getInstruction());
            return;
        }

//        添加单个连接
        String tName = content.trim().substring(sessionIdString.length()).trim();
        if (announcement.isMinecraftThreadExist(session, tName)) {
            announcement.delMinecraftThread(session, tName);
        } else {
            announcement.addMinecraftThread(session, tName);
        }

        subject.sendMessage(announcement.toOutputString(sessions));
        subject.sendMessage(step.getInstruction());
    }

    public void onAnnounceContent(McChatCommandStep step, Contact subject, User sender, String content) {
//        发送公告内容
        List<Session> sessions;
        try {
            sessions = SessionUtil.getUserSessions(sender.getId());
        } catch (Exception e) {
            e.printStackTrace();
            subject.sendMessage("发送公告内容设置在获取会话列表时产生异常，请稍后重试或联系开发者");
//            跳回到上一步，因为此处无法调用announcement.toOutputString
            McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.ANNOUNCE, subject);
            return;
        }

        Announcement announcement = announceTempMap.get(sender.getId());

        if ("exit".equalsIgnoreCase(content)) {
            subject.sendMessage(announcement.toOutputString(sessions));
            McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.ANNOUNCE, subject);
            return;
        }

        announcement.setContent(content);
        subject.sendMessage(announcement.toOutputString(sessions));
        McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.ANNOUNCE, subject);
    }

    public void onAnnounceOk(McChatCommandStep step, Contact subject, User sender, String content) {
//        确认发送公告
        List<Session> sessions;
        try {
            sessions = SessionUtil.getUserSessions(sender.getId());
        } catch (Exception e) {
            e.printStackTrace();
            subject.sendMessage("发送公告过程中在获取会话列表时产生异常，请稍后重试或联系开发者");
            McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.ANNOUNCE, subject);
            return;
        }

        Announcement announcement = announceTempMap.get(sender.getId());
        if (announcement.getContent() == null || announcement.getContent().equals("")) {
            subject.sendMessage("未指定公告内容");
            subject.sendMessage(step.getInstruction());
            return;
        }

        if (SessionModifyUtil.isSessionModifying) {
            subject.sendMessage("当前正有其它重要操作正在执行，执行完毕后开始发送公告");
        }

        synchronized (SessionModifyUtil.sessionModifyLock) {
            SessionModifyUtil.isSessionModifying = true;

            for (Session session : sessions) {
                List<String> threadNames = announcement.getThreadsMap().get(session);
                for (String threadName : threadNames) {
                    try {
                        session.sendAnnouncementToMinecraftConnection(sender.getId(), sender.getNick(), threadName, announcement.getContent());
                        subject.sendMessage("已向会话" + session.getId() + "的连接" + threadName + "发送公告内容");
                    } catch (MinecraftThreadNotFoundException e) {
                        subject.sendMessage("会话" + session.getId() + "不存在连接" + threadName);
                    }
                }
            }

            SessionModifyUtil.isSessionModifying = false;
        }

        McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.MAIN, subject);
    }

    public void onUserCommand(McChatCommandStep step, Contact subject, User sender, String content) {
//        用户指令相关设置
        if ("exit".equalsIgnoreCase(content)) {
            McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.MAIN, subject);
            return;
        }

        if ("list".equalsIgnoreCase(content)) {
//            发送list
            List<Session> userSessions;
            try {
                userSessions = SessionUtil.getUserSessions(sender.getId());
            } catch (Exception e) {
                e.printStackTrace();
                subject.sendMessage("发生异常，请联系管理原或稍后重试");
                subject.sendMessage(step.getInstruction());
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("连接列表：\n");
            for (Session session : userSessions) {
                sb.append("    会话 ").append(session.getId()).append("(").append(session.getName()).append(")：\n");

                List<MinecraftConnectionThread> minecraftThreads = session.getMinecraftThreads();
                for (MinecraftConnectionThread thread : minecraftThreads) {
                    sb.append("        ").append(thread.getServerName().getContent()).append("(").append(thread.getServerAddress()).append(")\n");
                }
            }

            subject.sendMessage(sb.toString());
            subject.sendMessage(step.getInstruction());
            return;
        }

        content = content.trim();
        String[] split = content.split(" ");

        if (split.length < 2) {
//            小于两个参数，则参数不全
            subject.sendMessage("参数不全");
            subject.sendMessage(step.getInstruction());
            return;
        }

//        获取输入会话号
        String sessionIdString = split[0];
        long sessionId;
        try {
            sessionId = Long.parseLong(sessionIdString);
        } catch (NumberFormatException e) {
            subject.sendMessage("会话号应为数字而不是" + sessionIdString);
            subject.sendMessage(step.getInstruction());
            return;
        }

//        检查会话存在
        Session session;
        try {
            session = SessionUtil.getUserSession(sessionId, sender.getId());
        } catch (SessionDataNotExistException e) {
            subject.sendMessage("会话号为" + sessionId + "的会话不存在");
            subject.sendMessage(step.getInstruction());
            return;
        } catch (Exception e) {
            e.printStackTrace();
            subject.sendMessage("发生异常，请联系管理原或稍后重试");
            subject.sendMessage(step.getInstruction());
            return;
        }

//        获取输入的服务器名字
        String serverName = content.substring(sessionIdString.length()).trim();
        boolean isExist = session.isMinecraftServerNameExist(serverName);

        UserCommand userCommand = new UserCommand();
        userCommand.setSessionId(session.getId());
        userCommand.setSessionName(session.getName());
        userCommand.setServerName(serverName);

        userCommandTempMap.put(sender.getId(), userCommand);

        subject.sendMessage(userCommand.toString());
        if (!isExist) {
            subject.sendMessage("会话 " + sessionId + "(" + session.getName() + ")" + " 的连接 " + serverName + " 不存在\n" +
                    "可能原因为：\n" +
                    "    连接名输入错误\n" +
                    "    此刻连接名为此名的连接暂时未接入\n" +
                    "可继续操作，将在发送确认指令时再次确认连接状态");
        }
        McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.USER_COMMAND_MENU, subject);
    }

    public void onUserCommandMenu(McChatCommandStep step, Contact subject, User sender, String content) {
//        用户指令主菜单
        if ("exit".equalsIgnoreCase(content)) {
            McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.MAIN, subject);
            return;
        }

        UserCommand userCommand = userCommandTempMap.get(sender.getId());

        switch (content.toLowerCase()) {
            case "add": {
//                添加用户指令
                UserCommandAdd uca = new UserCommandAdd();
                uca.setSessionId(userCommand.getSessionId());
                uca.setSessionName(userCommand.getSessionName());
                uca.setServerName(userCommand.getServerName());
                userCommandAddTempMap.put(sender.getId(), uca);

                subject.sendMessage(uca.toString());
                McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.USER_COMMAND_ADD_NAME, subject);
                break;
            }
            case "del": {
//                删除用户指令
                UserCommandDel ucd = new UserCommandDel();
                ucd.setSessionId(userCommand.getSessionId());
                ucd.setSessionName(userCommand.getSessionName());
                ucd.setServerName(userCommand.getServerName());
                userCommandDelTempMap.put(sender.getId(), ucd);

                subject.sendMessage(ucd.toString());
                McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.USER_COMMAND_DEL, subject);
                break;
            }
            case "list": {
//                列出用户指令
                Session session;
                try {
                    session = SessionUtil.getUserSession(userCommand.getSessionId(), sender.getId());
                } catch (SessionDataNotExistException e) {
                    subject.sendMessage("会话号为" + userCommand.getSessionId() + "的会话不存在");
                    subject.sendMessage(userCommand.toString());
                    subject.sendMessage(step.getInstruction());
                    break;
                } catch (Exception e) {
                    subject.sendMessage("出现其它异常，请稍后重试或联系开发者");
                    subject.sendMessage(userCommand.toString());
                    subject.sendMessage(step.getInstruction());
                    break;
                }

                MinecraftConnectionThread thread = session.getMinecraftThread(userCommand.getServerName());
                if (thread == null) {
                    subject.sendMessage("会话号为" + userCommand.getSessionId() + "的会话中不存在名称为" + userCommand.getServerName() + "的连接");
                    subject.sendMessage(userCommand.toString());
                    subject.sendMessage(step.getInstruction());
                    break;
                }

                thread.sendGetMcChatUserCommands(sender.getId());
                subject.sendMessage("用户指令列表将在异步消息中返回");
                subject.sendMessage(userCommand.toString());
                subject.sendMessage(step.getInstruction());
                break;
            }

            default:
                subject.sendMessage("不存在的指令");
                subject.sendMessage(userCommand.toString());
                subject.sendMessage(step.getInstruction());
                return;
        }
    }

    public void onUserCommandAddName(McChatCommandStep step, Contact subject, User sender, String content) {
//        添加用户指令 指令名
        UserCommand userCommand = userCommandTempMap.get(sender.getId());

        if ("exit".equalsIgnoreCase(content)) {
            subject.sendMessage(userCommand.toString());
            McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.USER_COMMAND_MENU, subject);
            return;
        }

        UserCommandAdd userCommandAdd = userCommandAddTempMap.get(sender.getId());

        if ("list".equalsIgnoreCase(content)) {
            subject.sendMessage("不能使用list作为指令名");
            subject.sendMessage(userCommandAdd.toString());
            subject.sendMessage(step.getInstruction());
            return;
        }

        if ("ok".equalsIgnoreCase(content)) {
            subject.sendMessage("不能使用ok作为指令名");
            subject.sendMessage(userCommandAdd.toString());
            subject.sendMessage(step.getInstruction());
            return;
        }

        userCommandAdd.setName(content);

        subject.sendMessage(userCommandAdd.toString());
        McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.USER_COMMAND_ADD_COMMAND, subject);
    }

    public void onUserCommandAddCommand(McChatCommandStep step, Contact subject, User sender, String content) {
//        添加用户指令 用户指令
        UserCommand userCommand = userCommandTempMap.get(sender.getId());
        UserCommandAdd userCommandAdd = userCommandAddTempMap.get(sender.getId());

        if ("exit".equalsIgnoreCase(content)) {
            subject.sendMessage(userCommand.toString());
            McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.USER_COMMAND_MENU, subject);
            return;
        }

        userCommandAdd.setCommand(content);
        List<String> commandArgList = userCommandAdd.getCommandArgList();
        StringBuilder sb = new StringBuilder("用户指令中具有参数：\n");
        for (String arg : commandArgList) {
            sb.append(arg).append("\n");
        }
        sb.append("指定实际指令时需使用以上所有参数");

        subject.sendMessage(userCommandAdd.toString());
        subject.sendMessage(sb.toString());
        McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.USER_COMMAND_ADD_MAPPING, subject);
    }

    public void onUserCommandAddMapping(McChatCommandStep step, Contact subject, User sender, String content) {
//        添加用户指令 实际指令
        UserCommand userCommand = userCommandTempMap.get(sender.getId());

        if ("exit".equalsIgnoreCase(content)) {
            subject.sendMessage(userCommand.toString());
            McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.USER_COMMAND_MENU, subject);
            return;
        }

        UserCommandAdd uca = userCommandAddTempMap.get(sender.getId());
        List<String> commandArgList = uca.getCommandArgList();

        uca.setMapping(content);
        List<String> mappingArgList = uca.getMappingArgList();

//        检查未使用的参数
        for (String s : mappingArgList) {
            commandArgList.removeIf(o -> o.equals(s));
        }

        if (commandArgList.size() != 0) {
            StringBuilder res = new StringBuilder("以下参数未使用：\n");
            for (String s : commandArgList) {
                res.append(s).append("\n");
            }

            uca.setMapping(null);
            subject.sendMessage(res.toString());
            subject.sendMessage(uca.toString());
            subject.sendMessage(step.getInstruction());
            return;
        }

//        检查未指定的参数
        commandArgList = uca.getCommandArgList();
        mappingArgList = uca.getMappingArgList();

        for (String s : commandArgList) {
            mappingArgList.removeIf(o -> o.equals(s));
        }

        if (mappingArgList.size() != 0) {
            StringBuilder res = new StringBuilder("以下参数未指定：\n");
            for (String s : mappingArgList) {
                res.append(s).append("\n");
            }

            uca.setMapping(null);
            subject.sendMessage(res.toString());
            subject.sendMessage(uca.toString());
            subject.sendMessage(step.getInstruction());
            return;
        }

        subject.sendMessage(uca.toString());
        McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.USER_COMMAND_ADD_CONFIRM, subject);
    }

    public void onUserCommandAddConfirm(McChatCommandStep step, Contact subject, User sender, String content) {
//        添加用户指令 确认

        UserCommand userCommand = userCommandTempMap.get(sender.getId());
        UserCommandAdd userCommandAdd = userCommandAddTempMap.get(sender.getId());

        switch (content.toLowerCase()) {
            case "exit":
                subject.sendMessage(userCommand.toString());
                McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.USER_COMMAND_MENU, subject);
                break;
            case "ok": {
                Session session;
                try {
                    session = SessionUtil.getSession(userCommandAdd.getSessionId());
                } catch (SessionDataNotExistException e) {
                    subject.sendMessage("会话号为" + userCommandAdd.getSessionId() + "的会话不存在");
                    subject.sendMessage(userCommandAdd.toString());
                    subject.sendMessage(step.getInstruction());
                    return;
                } catch (Exception e) {
                    subject.sendMessage("出现其它异常，请稍后重试或者联系开发者");
                    subject.sendMessage(userCommandAdd.toString());
                    subject.sendMessage(step.getInstruction());
                    return;
                }

                MinecraftConnectionThread minecraftThread = session.getMinecraftThread(userCommandAdd.getServerName());
                if (minecraftThread == null) {
                    subject.sendMessage("会话 " + userCommandAdd.getSessionId() + " 没有连接名为 " + userCommandAdd.getServerName() + " 的连接");
                    subject.sendMessage(userCommandAdd.toString());
                    subject.sendMessage(step.getInstruction());
                    return;
                }

                minecraftThread.sendAddUserCommandPacket(
                        sender.getId(),
                        userCommandAdd.getName(),
                        userCommandAdd.getCommand(),
                        userCommandAdd.getMapping()
                );

                subject.sendMessage("已发送添加用户指令数据包，执行结果将在另一条异步消息返回");
                McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.USER_COMMAND_MENU, subject);
                break;
            }
            default:
                subject.sendMessage("无该指令");
                subject.sendMessage(userCommandAdd.toString());
                subject.sendMessage(step.getInstruction());
                break;
        }
    }

    public void onUserCommandDelCommand(McChatCommandStep step, Contact subject, User sender, String content) {
//        删除用户指令
        UserCommand userCommand = userCommandTempMap.get(sender.getId());
        UserCommandDel userCommandDel = userCommandDelTempMap.get(sender.getId());
        if ("exit".equalsIgnoreCase(content)) {
            subject.sendMessage(userCommand.toString());
            McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.USER_COMMAND_MENU, subject);
            return;
        }

//        先获取会话和连接
        Session session;
        try {
            session = SessionUtil.getUserSession(userCommandDel.getSessionId(), sender.getId());
        } catch (SessionDataNotExistException e) {
            subject.sendMessage("会话号为" + userCommandDel.getSessionId() + "的会话不存在");
            subject.sendMessage(userCommandDel.toString());
            subject.sendMessage(step.getInstruction());
            return;
        } catch (Exception e) {
            subject.sendMessage("出现其它异常，请稍后重试或者联系开发者");
            subject.sendMessage(userCommandDel.toString());
            subject.sendMessage(step.getInstruction());
            return;
        }

        MinecraftConnectionThread minecraftThread = session.getMinecraftThread(userCommandDel.getServerName());
        if (minecraftThread == null) {
            subject.sendMessage("会话 " + userCommandDel.getSessionId() + " 没有连接名为 " + userCommandDel.getServerName() + " 的连接");
            subject.sendMessage(userCommandDel.toString());
            subject.sendMessage(step.getInstruction());
            return;
        }

        if ("list".equalsIgnoreCase(content)) {
            minecraftThread.sendGetMcChatUserCommands(sender.getId());
            subject.sendMessage("已发送获取用户指令数据包，返回结果在另一条消息");
            subject.sendMessage(userCommandDel.toString());
            subject.sendMessage(step.getInstruction());
            return;
        }

        if ("ok".equalsIgnoreCase(content)) {
            List<String> delNames = userCommandDel.getDelNames();
            for (String delName : delNames) {
                minecraftThread.sendDelUserCommandPacket(sender.getId(), delName);
            }
            subject.sendMessage("已发送删除用户指令数据包，返回结果在另一条消息");
            McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.USER_COMMAND_MENU, subject);
            return;
        }

        userCommandDel.selectDelName(content);
        subject.sendMessage(userCommandDel.toString());
        subject.sendMessage(step.getInstruction());
    }
}
