package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.listener;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.data.SessionDataOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.SessionDataExistException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.SessionDataNotExistException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.Session;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.MessageUtil;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.SessionUtil;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.opmcchatcommand.OpMcChatCommandStep;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.opmcchatcommand.OpMcChatCommandStepUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListenerHost;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpMcCommandStepListener implements ListenerHost {

    //    临时存放新会话号
    private final static Map<Long, Long> newSessionIdTempMap = new HashMap<>();
    //    临时存放新会话名
    private final static Map<Long, String> newSessionNameTempMap = new HashMap<>();
    //    临时存放群互通消息格式
    private final static Map<Long, String> newSessionGroupFormatTempMap = new HashMap<>();
    //    临时存放即将删除的会话号
    private final static Map<Long, Long> delSessionIdTempMap = new HashMap<>();
    //    忙碌状态，为true时拒绝任何会话操作
    private static boolean isBusy = false;

    @EventHandler
    public void onMessage(MessageEvent e) {
        MessageChain message = e.getMessage();
        String content = message.contentToString();
        Contact subject = e.getSubject();
        User sender = e.getSender();

        OpMcChatCommandStep step = OpMcChatCommandStepUtil.getStep(sender.getId());
        if (step == null) {
            return;
        }

        if (content.equalsIgnoreCase("quit")) {
            OpMcChatCommandStepUtil.clearStep(sender.getId(), subject, "已退出指令");
            return;
        }

        if (isBusy) {
            subject.sendMessage("会话管理正在执行重要操作，暂时拒绝操作，请稍后重试");
            return;
        }

        switch (step) {
            case MAIN:
                onMainMenu(step, subject, sender, content);
                break;
            case MAIN_LIST:
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
        }
    }

    public void onMainMenu(OpMcChatCommandStep step, Contact subject, User sender, String content) {
        switch (content.toLowerCase()) {
            case "list":
//                查看会话信息
                OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MAIN_LIST, subject);
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
                allSessionInfo.append("主菜单/查看会话信息/所有会话号：\n");
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

        String sb = "主菜单/查看会话信息/会话" + sessionId + "：\n" +
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

                subject.sendMessage("开始添加该新会话，请稍等");
//                设置为忙碌状态
                isBusy = true;
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
                isBusy = false;

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

        try {
            SessionUtil.getSession(delSessionId);
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

        delSessionIdTempMap.put(sender.getId(), delSessionId);
        subject.sendMessage("即将删除的会话号：" + delSessionIdTempMap.get(sender.getId()));
        OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.DEL_CONFIRM, subject);
    }

    public void onDelConfirm(OpMcChatCommandStep step, Contact subject, User sender, String content) {
//        删除会话确认
        switch (content.toLowerCase()) {
            case "ok":
                subject.sendMessage("开始删除会话，请稍等");
                isBusy = true;
                try {
                    SessionDataOperation.removeSessionData(delSessionIdTempMap.get(sender.getId()));
                } catch (SessionDataNotExistException e) {
                    subject.sendMessage("会话号" + delSessionIdTempMap.get(sender.getId()) + "不存在，本次删除失败");
                    subject.sendMessage(step.getInstruction());
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    subject.sendMessage("出现其它异常，请稍后重试或联系开发者");
                    subject.sendMessage(step.getInstruction());
                    return;
                }
                subject.sendMessage("删除会话完成");
                isBusy = false;

                OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MAIN, subject);
                break;
            case "exit":
                OpMcChatCommandStepUtil.setStep(sender.getId(), OpMcChatCommandStep.MAIN, subject);
                break;
            default:
                subject.sendMessage("没有该指令");
                subject.sendMessage("即将删除的会话号：" + delSessionIdTempMap.get(sender.getId()));
                subject.sendMessage(step.getInstruction());
                break;
        }
    }
}
