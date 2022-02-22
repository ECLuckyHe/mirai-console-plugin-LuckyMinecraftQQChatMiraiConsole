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
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.opmcchatcommand.OpMcChatCommandStep;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.opmcchatcommand.OpMcChatCommandStepUtil;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.pojo.Announcement;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListenerHost;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OpMcCommandStepListener implements ListenerHost {

    private LuckyMinecraftQQChatMiraiConsole INSTANCE;

    //    临时存放新会话号
    private final static Map<Long, Long> newSessionIdTempMap = new HashMap<>();
    //    临时存放新会话名
    private final static Map<Long, String> newSessionNameTempMap = new HashMap<>();
    //    临时存放群互通消息格式
    private final static Map<Long, String> newSessionGroupFormatTempMap = new HashMap<>();
    //    临时存放即将删除的会话
    private final static Map<Long, Session> delSessionIdTempMap = new HashMap<>();
    //    临时存放正在进行操作的会话（该会话是复制的，不会影响到正在运行的会话）
    private final static Map<Long, Session> modifySessionIdTempMap = new HashMap<>();
    //    临时存放正在发送的公告对象
    private final static Map<Long, Announcement> announceTempMap = new HashMap<>();

    public OpMcCommandStepListener(LuckyMinecraftQQChatMiraiConsole INSTANCE) {
        this.INSTANCE = INSTANCE;
    }

    @EventHandler
    public void onFriendMessage(FriendMessageEvent e) {
        String commandPrefix = CommandManager.INSTANCE.getCommandPrefix();
        MessageChain message = e.getMessage();
        String content = message.contentToString();
        Contact subject = e.getSubject();
        User sender = e.getSender();

        if ((commandPrefix + INSTANCE.getOpMcChatCommandPrimaryName()).equalsIgnoreCase(content)) {
            return;
        }

        for (String secondaryName : INSTANCE.getOpMcChatCommandSecondaryNames()) {
            if ((commandPrefix + secondaryName).equalsIgnoreCase(content)) {
                return;
            }
        }

        OpMcChatCommandStep step = OpMcChatCommandStepUtil.getStep(sender.getId());
        if (step == null) {
            return;
        }

        if ("quit".equalsIgnoreCase(content)) {
            OpMcChatCommandStepUtil.clearStep(sender.getId(), subject, "已退出指令");
            return;
        }

        if (SessionModifyUtil.isSessionModifying) {
            subject.sendMessage("会话管理正在执行重要操作，暂时拒绝操作，请稍后重试");
            return;
        }

        switch (step) {
            case MAIN:
                onMainMenu(step, subject, sender, content);
                break;
            case LIST:
                onList(step, subject, sender, content);
                break;
            case ADD:
                onAdd(step, subject, sender, content);
                break;
            case ADD_SESSION_NAME:
                onAddSessionName(step, subject, sender, content);
                break;
            case ADD_GROUP_FORMAT:
                onAddGroupFormat(step, subject, sender, content);
                break;
            case ADD_CONFIRM:
                onAddConfirm(step, subject, sender, content);
                break;
            case DEL:
                onDel(step, subject, sender, content);
                break;
            case DEL_CONFIRM:
                onDelConfirm(step, subject, sender, content);
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
            case MODIFY_ADD_ADMINISTRATOR:
                onModifyAddAdministrator(step, subject, sender, content);
                break;
            case MODIFY_DEL_ADMINISTRATOR:
                onModifyDelAdministrator(step, subject, sender, content);
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
        }
    }

    public void onMainMenu(OpMcChatCommandStep step, Contact subject, User sender, String content) {
        switch (content.toLowerCase()) {
            case "list":
//                查看会话信息
                OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.LIST, subject);
                break;
            case "add":
//                添加会话信息
                OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.ADD, subject);
                break;
            case "del":
//                删除会话信息
                OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.DEL, subject);
                break;
            case "modify":
//                修改会话信息
                OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MODIFY, subject);
                break;
            case "announce":
//                公告
                Announcement announcement = new Announcement();
                try {
                    subject.sendMessage(announcement.toOutputString(SessionUtil.getSessions()));
                } catch (Exception e) {
                    subject.sendMessage("在获取会话列表时产生异常，请稍后重试或联系开发者");
                    subject.sendMessage(step.getInstruction());
                    return;
                }
                announceTempMap.put(sender.getId(), announcement);
                OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.ANNOUNCE, subject);
                break;
            case "exit":
                OpMcChatCommandStepUtil.clearStep(sender.getId(), subject, "已退出指令");
                break;
            default:
                subject.sendMessage("无该指令");
                subject.sendMessage(step.getInstruction());
                break;
        }
    }

    public void onList(OpMcChatCommandStep step, Contact subject, User sender, String content) {
//        会话号列表指令
        switch (content.toLowerCase()) {
            case "all":
//                查看所有会话号
                StringBuilder allSessionInfo = new StringBuilder();
                allSessionInfo.append("主菜单/查看会话信息/所有会话号\n");
                allSessionInfo.append("====================\n");
                allSessionInfo.append("会话号(会话名)\n");
                List<Session> sessions;
                try {
                    sessions = SessionUtil.getSessions();
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
            case "exit":
                OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MAIN, subject);
                return;
        }

//        此处以后为输入对应会话号查询
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
            session = SessionUtil.getSession(sessionId);
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

        String sb = "主菜单/查看会话信息/会话" + sessionId + "\n" +
                "====================\n" +
                SessionUtil.sessionToString(session);

        subject.sendMessage(sb);
        subject.sendMessage(step.getInstruction());
    }

    public void onAdd(OpMcChatCommandStep step, Contact subject, User sender, String content) {
//        添加会话操作
        if ("exit".equalsIgnoreCase(content)) {
            OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MAIN, subject);
            return;
        }

//        以下为判断新会话操作
        long newSessionId;
        try {
            newSessionId = Long.parseLong(content);
        } catch (NumberFormatException e) {
            subject.sendMessage("新会话号应为数字，而不是" + content);
            subject.sendMessage(step.getInstruction());
            return;
        }

        try {
            SessionUtil.getSession(newSessionId);
//            如果会话号存在，则会执行下方语句，否则将抛出异常
            subject.sendMessage("会话号" + newSessionId + "已存在");
            subject.sendMessage(step.getInstruction());
            return;
        } catch (SessionDataNotExistException ignored) {

        } catch (Exception e) {
            e.printStackTrace();
            subject.sendMessage("出现其它异常，请稍后重试或联系开发者");
            subject.sendMessage(step.getInstruction());
            return;
        }

//        下一步操作
        newSessionIdTempMap.put(sender.getId(), newSessionId);
        OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.ADD_SESSION_NAME, subject);
    }

    public void onAddSessionName(OpMcChatCommandStep step, Contact subject, User sender, String content) {
        if ("exit".equalsIgnoreCase(content)) {
            OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MAIN, subject);
            return;
        }

//        以下为会话名输入
        newSessionNameTempMap.put(sender.getId(), content);
        OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.ADD_GROUP_FORMAT, subject);
    }

    public void onAddGroupFormat(OpMcChatCommandStep step, Contact subject, User sender, String content) {
//        输入群互通消息格式
        if ("exit".equalsIgnoreCase(content)) {
            OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MAIN, subject);
            return;
        }

//        以下为自定义互通消息格式
        String groupFormat = "[%groupNickname%] <%senderGroupNickname%> %message%";
        if (!content.equalsIgnoreCase("default")) {
            groupFormat = content;
        }
        newSessionGroupFormatTempMap.put(sender.getId(), groupFormat);

//        输出确认信息
        subject.sendMessage("新会话号：" + newSessionIdTempMap.get(sender.getId()) + "\n" +
                "会话名：" + newSessionNameTempMap.get(sender.getId()) + "\n" +
                "群互通消息格式：" + newSessionGroupFormatTempMap.get(sender.getId()));
        OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.ADD_CONFIRM, subject);
    }

    public void onAddConfirm(OpMcChatCommandStep step, Contact subject, User sender, String content) {
//        添加会话确认
        switch (content.toLowerCase()) {
            case "ok":
                if (SessionModifyUtil.isSessionModifying) {
                    subject.sendMessage("当前正有其它重要操作正在执行，执行完毕后开始添加新会话");
                }
                synchronized (SessionModifyUtil.sessionModifyLock) {
                    //                设置为忙碌状态
                    SessionModifyUtil.isSessionModifying = true;
                    subject.sendMessage("开始添加该新会话，请稍等");
                    try {
                        SessionDataOperation.addSessionData(
                                newSessionIdTempMap.get(sender.getId()),
                                newSessionNameTempMap.get(sender.getId()),
                                newSessionGroupFormatTempMap.get(sender.getId())
                        );
                    } catch (SessionDataExistException e) {
                        subject.sendMessage("会话号" + newSessionIdTempMap.get(sender.getId()) + "已存在，本次添加失败");
                        subject.sendMessage(step.getInstruction());
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        subject.sendMessage("出现其它异常，请稍后重试或联系开发者");
                        subject.sendMessage(step.getInstruction());
                        return;
                    }
                    subject.sendMessage("会话已经成功添加");
//                解除忙碌状态
                    SessionModifyUtil.isSessionModifying = false;
                }

                OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MAIN, subject);
                break;
            case "exit":
                OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MAIN, subject);
                return;
            default:
                subject.sendMessage("没有该指令");
                subject.sendMessage("新会话号：" + newSessionIdTempMap.get(sender.getId()) + "\n" +
                        "会话名：" + newSessionNameTempMap.get(sender.getId()) + "\n" +
                        "群互通消息格式：" + newSessionGroupFormatTempMap.get(sender.getId()));
                subject.sendMessage(step.getInstruction());
                break;
        }
    }

    public void onDel(OpMcChatCommandStep step, Contact subject, User sender, String content) {
//        删除会话信息
        if ("exit".equalsIgnoreCase(content)) {
            OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MAIN, subject);
            return;
        }

        long delSessionId;
        try {
            delSessionId = Long.parseLong(content);
        } catch (NumberFormatException e) {
            subject.sendMessage("会话号应为数字而不是" + content);
            subject.sendMessage(step.getInstruction());
            return;
        }

        Session session;
        try {
            session = SessionUtil.getSession(delSessionId);
        } catch (SessionDataNotExistException e) {
            subject.sendMessage("没有会话号为" + delSessionId + "的会话");
            subject.sendMessage(step.getInstruction());
            return;
        } catch (Exception e) {
            e.printStackTrace();
            subject.sendMessage("出现其它异常，请稍后重试或联系开发者");
            subject.sendMessage(step.getInstruction());
            return;
        }

        delSessionIdTempMap.put(sender.getId(), session);
        subject.sendMessage("即将删除的会话号：" + SessionUtil.sessionToString(delSessionIdTempMap.get(sender.getId())));
        OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.DEL_CONFIRM, subject);
    }

    public void onDelConfirm(OpMcChatCommandStep step, Contact subject, User sender, String content) {
//        删除会话确认
        switch (content.toLowerCase()) {
            case "ok":
                if (SessionModifyUtil.isSessionModifying) {
                    subject.sendMessage("当前正有其它重要操作正在执行，执行完毕后开始删除会话");
                }

                synchronized (SessionModifyUtil.sessionModifyLock) {
                    SessionModifyUtil.isSessionModifying = true;
                    subject.sendMessage("开始删除会话，请稍等");
                    try {
                        SessionDataOperation.removeSessionData(delSessionIdTempMap.get(sender.getId()).getId());
                    } catch (SessionDataNotExistException e) {
                        subject.sendMessage("会话号" + delSessionIdTempMap.get(sender.getId()).getId() + "不存在，本次删除失败");
                        subject.sendMessage(step.getInstruction());
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        subject.sendMessage("出现其它异常，请稍后重试或联系开发者");
                        subject.sendMessage(step.getInstruction());
                        return;
                    }
                    subject.sendMessage("删除会话完成");
                    SessionModifyUtil.isSessionModifying = false;
                }

                OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MAIN, subject);
                break;
            case "exit":
                OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MAIN, subject);
                break;
            default:
                subject.sendMessage("没有该指令");
                subject.sendMessage("即将删除的会话号：" + SessionUtil.sessionToString(delSessionIdTempMap.get(sender.getId())));
                subject.sendMessage(step.getInstruction());
                break;
        }
    }

    public void onModify(OpMcChatCommandStep step, Contact subject, User sender, String content) {
//        修改会话输入会话号
        if ("exit".equalsIgnoreCase(content)) {
            OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MAIN, subject);
            return;
        }

        if ("list".equalsIgnoreCase(content)) {
            //                查看所有会话号
            StringBuilder allSessionInfo = new StringBuilder();
            allSessionInfo.append("主菜单/查看会话信息/所有会话号\n");
            allSessionInfo.append("====================\n");
            allSessionInfo.append("会话号(会话名)\n");
            List<Session> sessions;
            try {
                sessions = SessionUtil.getSessions();
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
            session = SessionUtil.getSession(modifySessionId);
        } catch (SessionDataNotExistException e) {
            subject.sendMessage("没有会话号为" + modifySessionId + "的会话");
            subject.sendMessage(step.getInstruction());
            return;
        } catch (Exception e) {
            subject.sendMessage("出现其它异常，请稍后重试或联系开发者");
            subject.sendMessage(step.getInstruction());
            return;
        }

//        复制一个对话
        Session copiedSession = SessionUtil.copySessionWithNoThreads(session);
        modifySessionIdTempMap.put(sender.getId(), copiedSession);
        subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(copiedSession));
        OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MODIFY_MAIN, subject);
    }

    public void onModifyMain(OpMcChatCommandStep step, Contact subject, User sender, String content) {
//        修改会话操作
        Session tempSession = modifySessionIdTempMap.get(sender.getId());

        switch (content.toLowerCase()) {
            case "exit":
                OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MAIN, subject);
                break;
            case "gadd":
                subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
                OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MODIFY_ADD_GROUP, subject);
                break;
            case "gdel":
                subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
                OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MODIFY_DEL_GROUP, subject);
                break;
            case "format":
                subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
                OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MODIFY_FORMAT, subject);
                break;
            case "name":
                subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
                OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MODIFY_SESSION_NAME, subject);
                break;
            case "aadd":
                subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
                OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MODIFY_ADD_ADMINISTRATOR, subject);
                break;
            case "adel":
                subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
                OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MODIFY_DEL_ADMINISTRATOR, subject);
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

    public void onModifyAddGroup(OpMcChatCommandStep step, Contact subject, User sender, String content) {
//        添加互通群
        Session tempSession = modifySessionIdTempMap.get(sender.getId());

        if ("exit".equalsIgnoreCase(content)) {
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MODIFY_MAIN, subject);
            return;
        }

//        以下为新互通群号和备注
        String[] split = content.split(" ");
        if (split.length < 2) {
            subject.sendMessage("不符合要求的格式");
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            subject.sendMessage(step.getInstruction());
            return;
        }

//        新群号
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

//        新群昵称
        String newGroupNickname = content.substring(newGroupIdString.length()).trim();
        if (newGroupNickname.equalsIgnoreCase("")) {
            subject.sendMessage("群名不应为空");
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            subject.sendMessage(step.getInstruction());
            return;
        }

//        查看群号是否已存在
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

    public void onModifyDelGroup(OpMcChatCommandStep step, Contact subject, User sender, String content) {
//        删除互通群
        Session tempSession = modifySessionIdTempMap.get(sender.getId());

        if ("exit".equalsIgnoreCase(content)) {
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MODIFY_MAIN, subject);
            return;
        }

//        要删除的群号
        long delGroupId;
        try {
            delGroupId = Long.parseLong(content);
        } catch (NumberFormatException e) {
            subject.sendMessage("群号应为数字而不是" + content);
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            subject.sendMessage(step.getInstruction());
            return;
        }

//        不存在该群
        if (!tempSession.hasGroup(delGroupId)) {
            subject.sendMessage("群" + delGroupId + "不存在于会话中");
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            subject.sendMessage(step.getInstruction());
            return;
        }

//        删除操作
        tempSession.getGroups().removeIf(s -> s.getId() == delGroupId);
        subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
        subject.sendMessage(step.getInstruction());
    }

    public void onModifyFormat(OpMcChatCommandStep step, Contact subject, User sender, String content) {
//        修改互通格式
        Session tempSession = modifySessionIdTempMap.get(sender.getId());

        String newFormat = null;
        switch (content.toLowerCase()) {
            case "exit":
                subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(modifySessionIdTempMap.get(sender.getId())));
                OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MODIFY_MAIN, subject);
                return;
            case "default":
                newFormat = "[%groupNickname%] <%senderGroupNickname%> %message%";
                break;
        }

//        自定义格式
        if (newFormat == null) {
            newFormat = content;
        }

        tempSession.setFormatString(newFormat);
        subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
        OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MODIFY_MAIN, subject);
    }

    public void onModifySessionName(OpMcChatCommandStep step, Contact subject, User sender, String content) {
//        修改会话名
        Session tempSession = modifySessionIdTempMap.get(sender.getId());

        if ("exit".equalsIgnoreCase(content)) {
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MODIFY_MAIN, subject);
            return;
        }

        tempSession.setName(content);
        subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
        OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MODIFY_MAIN, subject);
    }

    public void onModifyAddAdministrator(OpMcChatCommandStep step, Contact subject, User sender, String content) {
//        添加管理员
        Session tempSession = modifySessionIdTempMap.get(sender.getId());

        if ("exit".equalsIgnoreCase(content)) {
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MODIFY_MAIN, subject);
            return;
        }

        long newQq;
        try {
            newQq = Long.parseLong(content);
        } catch (NumberFormatException e) {
            subject.sendMessage("管理员QQ应该为数字而不是" + content);
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            subject.sendMessage(step.getInstruction());
            return;
        }

        tempSession.getAdministrators().add(newQq);
        subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
        subject.sendMessage(step.getInstruction());
    }

    public void onModifyDelAdministrator(OpMcChatCommandStep step, Contact subject, User sender, String content) {
//        删除管理员
        Session tempSession = modifySessionIdTempMap.get(sender.getId());

        if ("exit".equalsIgnoreCase(content)) {
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MODIFY_MAIN, subject);
            return;
        }

        long delQq;
        try {
            delQq = Long.parseLong(content);
        } catch (NumberFormatException e) {
            subject.sendMessage("管理员QQ应该为数字而不是" + content);
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            subject.sendMessage(step.getInstruction());
            return;
        }

        tempSession.getAdministrators().removeIf(o -> o == delQq);
        subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
        subject.sendMessage(step.getInstruction());
    }

    public void onModifyOk(OpMcChatCommandStep step, Contact subject, User sender, String content) {
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
                } catch (IOException e) {
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
                    } catch (IOException e) {
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
                } catch (IOException e) {
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
            subject.sendMessage("在获取修改后会话时出现其它异常，请稍后重试或联系开发者");
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            subject.sendMessage(step.getInstruction());
            return;
        }

        subject.sendMessage("对于会话" + tempSession.getId() + "的修改已完成\n" +
                "====================\n" + SessionUtil.sessionToString(modifiedSession));
        OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MAIN, subject);
    }

    public void onAnnounce(OpMcChatCommandStep step, Contact subject, User sender, String content) {
//        发送公告

        if ("exit".equalsIgnoreCase(content)) {
            OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MAIN, subject);
            return;
        }

        List<Session> sessions;
        try {
            sessions = SessionUtil.getSessions();
        } catch (Exception e) {
            subject.sendMessage("发送公告功能在获取会话列表时产生异常，请稍后重试或联系开发者");
            subject.sendMessage(step.getInstruction());
            return;
        }

        Announcement announcement = announceTempMap.get(sender.getId());

        switch (content.toLowerCase()) {
            case "content":
                subject.sendMessage(announcement.toOutputString(sessions));
                OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.ANNOUNCE_CONTENT, subject);
                break;
            case "mc":
                subject.sendMessage(announcement.toOutputString(sessions));
                OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.ANNOUNCE_MC, subject);
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

    public void onAnnounceContent(OpMcChatCommandStep step, Contact subject, User sender, String content) {
//        发送公告内容
        List<Session> sessions;
        try {
            sessions = SessionUtil.getSessions();
        } catch (Exception e) {
            subject.sendMessage("发送公告内容设置在获取会话列表时产生异常，请稍后重试或联系开发者");
//            跳回到上一步，因为此处无法调用announcement.toOutputString
            OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.ANNOUNCE, subject);
            return;
        }

        Announcement announcement = announceTempMap.get(sender.getId());

        if ("exit".equalsIgnoreCase(content)) {
            subject.sendMessage(announcement.toOutputString(sessions));
            OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.ANNOUNCE, subject);
            return;
        }

        announcement.setContent(content);
        subject.sendMessage(announcement.toOutputString(sessions));
        OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.ANNOUNCE, subject);
    }

    public void onAnnounceMc(OpMcChatCommandStep step, Contact subject, User sender, String content) {
//        选定发送公告连接
        List<Session> sessions;
        try {
            sessions = SessionUtil.getSessions();
        } catch (Exception e) {
            subject.sendMessage("发送公告连接设置在获取会话列表时产生异常，请稍后重试或联系开发者");
            OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.ANNOUNCE, subject);
            return;
        }

        Announcement announcement = announceTempMap.get(sender.getId());

        if ("exit".equalsIgnoreCase(content)) {
            subject.sendMessage(announcement.toOutputString(sessions));
            OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.ANNOUNCE, subject);
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
            session = SessionUtil.getSession(sessionId);
        } catch (SessionDataNotExistException e) {
            subject.sendMessage("会话号为" + sessionId + "的会话不存在");
            subject.sendMessage(announcement.toOutputString(sessions));
            subject.sendMessage(step.getInstruction());
            return;
        } catch (Exception e) {
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

    public void onAnnounceOk(OpMcChatCommandStep step, Contact subject, User sender, String content) {
//        确认发送公告
        List<Session> sessions;
        try {
            sessions = SessionUtil.getSessions();
        } catch (Exception e) {
            subject.sendMessage("发送公告过程中在获取会话列表时产生异常，请稍后重试或联系开发者");
            OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.ANNOUNCE, subject);
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

        OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MAIN, subject);
    }
}
