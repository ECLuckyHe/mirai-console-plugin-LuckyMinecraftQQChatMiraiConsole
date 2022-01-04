package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils;

import net.mamoe.mirai.utils.MiraiLogger;

public class MiraiLoggerUtil {
    private static MiraiLogger logger;

    public static void init(MiraiLogger l) {
        logger = l;
    }

    public static MiraiLogger getLogger() {
        return logger;
    }
}
