package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.listener;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.opmcchatcommand.OpMcChatCommandStep;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.opmcchatcommand.OpMcChatCommandStepUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListenerHost;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;

public class OpMcCommandStepListener implements ListenerHost {

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

        switch (step) {
            case MAIN:
                onMainMenu(step, subject, sender, content);
                break;
        }
    }

    public void onMainMenu(OpMcChatCommandStep step, Contact subject, User sender, String content) {
        switch (content.toLowerCase()) {
            case "session":

                break;
            case "exit":
            case "quit":
                OpMcChatCommandStepUtil.clearStep(sender.getId(), subject, "已退出指令");
                break;
            default:
                subject.sendMessage(step.getInstruction());
                break;
        }
    }
}
