package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.opmcchatcommand;

import net.mamoe.mirai.contact.Contact;

import java.util.HashMap;
import java.util.Map;

public class OpMcChatCommandStepUtil {
    private static final Map<Long, OpMcChatCommandStep> stepMap = new HashMap<>();

    public static Map<Long, OpMcChatCommandStep> getAllStepMap() {
        return stepMap;
    }

    public static OpMcChatCommandStep getStep(Long qq) {
        return stepMap.get(qq);
    }

    public static void setStep(Long qq, OpMcChatCommandStep step, Contact contact) {
        stepMap.put(qq, step);
        contact.sendMessage(step.getInstruction());
    }

    public static void clearStep(Long qq, Contact contact, String exitMsg) {
        stepMap.put(qq, null);
        contact.sendMessage(exitMsg);
    }
}
