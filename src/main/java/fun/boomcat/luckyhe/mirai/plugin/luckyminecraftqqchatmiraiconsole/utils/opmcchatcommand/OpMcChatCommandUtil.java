package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.opmcchatcommand;

import net.mamoe.mirai.console.command.CommandManager;

public class OpMcChatCommandUtil {
    public static String mainHelp() {

        return "主菜单：\n" +
                "====================\n" +
                "操作：\n" +
                "list    查看会话信息\n" +
                "add    添加会话信息\n" +
                "del    删除会话信息\n" +
                "modify    修改会话信息\n" +
                "exit    返回到上一级（全局通用）\n" +
                "quit    退出指令";
    }

    public static String listHelp() {
        return "主菜单/查看会话信息：\n" +
                "====================\n" +
                "操作：\n" +
                "<会话号>    发送会话号以查看信息\n" +
                "all    查看所有会话号\n" +
                "exit    返回到上一级\n" +
                "quit    退出指令";
    }

    public static String addHelp() {
        return "主菜单/添加会话：\n" +
                "会话号为会话的唯一标识，以此来区分不同的会话\n" +
                "====================\n" +
                "操作：\n" +
                "<会话号>    输入一个新会话号（数字）以新建一个会话\n" +
                "exit    返回到上一级\n" +
                "quit    退出指令";
    }

    public static String addSessionNameHelp() {
        return "主菜单/添加会话/输入新会话名：\n" +
                "会话名仅仅用于备注该会话以方便查询\n" +
                "====================\n" +
                "操作：\n" +
                "<新会话名>    输入一个新会话名以新建一个会话\n" +
                "exit    退出新建会话操作\n" +
                "quit    退出指令";
    }

    public static String addGroupFormatHelp() {
        return "主菜单/添加会话/输入群与群互通消息格式：\n" +
                "该消息格式用于群与群之间的消息互通\n" +
                "同一个会话中的群共用一个消息格式\n" +
                "====================\n" +
                "以下为可用的占位符参数：\n" +
                "%sessionName%    会话名\n" +
                "%groupId%    群号\n" +
                "%groupName%    群名\n" +
                "%groupNickname%    群昵称（在添加群到会话中时指定）\n" +
                "%senderId%    发送者QQ\n" +
                "%senderNickname%    发送者昵称\n" +
                "%senderGroupNickname%    发送者群昵称\n" +
                "%message%    消息内容\n" +
                "====================\n" +
                "一个例子：\n" +
                "[%groupNickname%] <%senderGroupNickname%> %message%\n" +
                "====================\n" +
                "操作：\n" +
                "<消息格式>    群与群之间的互通消息格式\n" +
                "default    使用默认消息格式\n" +
                "exit    退出新建会话操作\n" +
                "quit    退出指令";
    }

    public static String addConfirmHelp() {
        return "主菜单/添加会话/确认新会话：\n" +
                "====================\n" +
                "操作：\n" +
                "ok    确认添加该会话\n" +
                "exit    退出新建会话操作\n" +
                "quit    退出指令";
    }

    public static String delHelp() {
        return "主菜单/删除会话：\n" +
                "====================\n" +
                "操作：\n" +
                "<会话号>    需要删除的会话号\n" +
                "exit    退出删除会话操作\n" +
                "quit    退出指令";
    }

    public static String delConfirm() {
        return "主菜单/删除会话/确认删除会话：\n" +
                "====================\n" +
                "操作：\n" +
                "ok    确认删除该会话\n" +
                "exit    退出删除会话操作\n" +
                "quit    退出指令";
    }
}
