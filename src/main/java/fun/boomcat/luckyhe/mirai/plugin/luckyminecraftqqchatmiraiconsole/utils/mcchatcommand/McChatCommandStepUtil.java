package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.mcchatcommand;

import net.mamoe.mirai.contact.Contact;

import java.util.HashMap;
import java.util.Map;

public class McChatCommandStepUtil {
    private static final Map<Long, McChatCommandStep> stepMap = new HashMap<>();

    public Map<Long, McChatCommandStep> getStepMap() {
        return stepMap;
    }

    public static McChatCommandStep getStep(Long qq) {
        return stepMap.get(qq);
    }

    public static void setStep(Long qq, McChatCommandStep step, Contact contact) {
        stepMap.put(qq, step);
        contact.sendMessage(step.getInstruction());
    }

    public static void clearStep(Long qq, Contact contact, String exitMsg) {
        stepMap.put(qq, null);
        contact.sendMessage(exitMsg);
    }
}
