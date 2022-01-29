package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils;

import net.mamoe.mirai.console.command.CommandManager;

public class OpMcChatCommandUtil {
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

    public static String sessionHelp(String primaryName, String[] secondaryNames) {
        StringBuilder sb = new StringBuilder();
        String commandPrefix = CommandManager.INSTANCE.getCommandPrefix();

        sb.append(commandPrefix).append(primaryName).append(" session 后接下列参数：\n");
        sb.append("list    输出当前所有会话信息\n");
        sb.append("list <会话号>    输出指定会话号信息\n");
        sb.append("add <会话号> <备注名> <消息格式（剩下所有参数）>    添加一个会话\n");
        sb.append("del <会话号>   删除一个会话\n");
        sb.append("modify    修改一个会话的信息\n");
        sb.append("announce    发送公告\n");

        sb.append("\n");
        sb.append("消息格式占位符如下：\n");
        sb.append("%sessionName%    会话备注\n");
        sb.append("%groupId%    群号\n");
        sb.append("%groupName%    群名\n");
        sb.append("%groupNickname%    群昵称（在添加到会话群列表时指定）\n");
        sb.append("%senderId%    发送者QQ\n");
        sb.append("%senderNickname%    发送者昵称\n");
        sb.append("%senderGroupNickname%    发送者群昵称\n");
        sb.append("%message%    消息内容\n");

        return sb.toString();
    }

    public static String modifyHelp(String primaryName, String[] secondaryNames) {
        StringBuilder sb = new StringBuilder();
        String commandPrefix = CommandManager.INSTANCE.getCommandPrefix();

        sb.append(commandPrefix).append(primaryName).append(" session modify\n");
        sb.append("后接<会话号>\n");
        sb.append("后接下列参数：\n");
        sb.append("groupadd <群号>    添加一个群号到该会话\n");
        sb.append("groupdel <群号>    从该会话删除一个群号\n");
        sb.append("name <新备注>    修改会话备注\n");
        sb.append("format <消息格式（剩下所有参数）>    修改消息格式\n");

        return sb.toString();
    }

    public static String announceHelp(String primaryName, String[] secondaryNames) {
        StringBuilder sb = new StringBuilder();
        String commandPrefix = CommandManager.INSTANCE.getCommandPrefix();

        sb.append(commandPrefix).append(primaryName).append(" session announce\n");
        sb.append("后接<会话号>\n");
        sb.append("后接下列参数：\n");
        sb.append("mc <MC端服务器名> <公告内容（后续所有参数）>    向指定MC端发送公告\n");
        sb.append("mcall <公告内容（后续所有参数）>    向所有MC端发送公告\n");

        return sb.toString();
    }
}
