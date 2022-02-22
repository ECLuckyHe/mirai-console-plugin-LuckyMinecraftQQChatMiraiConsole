package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.listener;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.LuckyMinecraftQQChatMiraiConsole;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.data.SessionDataOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.*;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.Session;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.SessionGroup;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.MessageUtil;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.SessionModifyUtil;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.SessionUtil;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.mcchatcommand.McChatCommandStep;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.mcchatcommand.McChatCommandStepUtil;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListenerHost;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class McCommandStepListener implements ListenerHost {
    private LuckyMinecraftQQChatMiraiConsole INSTANCE;
    //    临时存放正在进行操作的会话
    private final static Map<Long, Session> modifySessionIdTempMap = new HashMap<>();

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

        try {
            oldSession = SessionUtil.getSession(tempSession.getId());
        } catch (SessionDataNotExistException e) {
            subject.sendMessage("会话号为" + tempSession.getId() + "的会话不存在");
            subject.sendMessage("正在修改会话（此为副本）：\n" + SessionUtil.sessionToString(tempSession));
            subject.sendMessage(step.getInstruction());
            return;
        } catch (FileNotFoundException e) {
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
        McChatCommandStepUtil.setStep(sender.getId(), McChatCommandStep.MAIN, subject);
    }


}
