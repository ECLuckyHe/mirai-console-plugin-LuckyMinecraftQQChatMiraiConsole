package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils;

import net.mamoe.mirai.console.command.CommandManager;

public class McChatUtil {
    public static String mainHelp(String primaryName, String[] secondaryNames) {
        String commandPrefix = CommandManager.INSTANCE.getCommandPrefix();
        StringBuilder sb = new StringBuilder();



        return sb.toString();
    }
}
