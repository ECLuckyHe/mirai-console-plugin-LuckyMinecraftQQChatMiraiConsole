package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils;

import net.mamoe.mirai.console.command.CommandManager;

public class McChatCommandUtil {
    public static String mainHelp(String primaryName, String[] secondaryNames) {
        StringBuilder sb = new StringBuilder();
        String commandPrefix = CommandManager.INSTANCE.getCommandPrefix();

        sb.append(commandPrefix).append(primaryName).append("指令 ");
        if (secondaryNames.length != 0) {
            sb.append("别名：");
            for (String secondaryName : secondaryNames) {
                sb.append(commandPrefix).append(secondaryName).append(" ");
            }
        }

        sb.append("后接参数：\n");
        sb.append("session    与会话相关的操作\n");

        return sb.toString();
    }
}
