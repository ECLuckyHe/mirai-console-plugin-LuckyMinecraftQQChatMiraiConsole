package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.mcchatcommand;

public class McChatCommandUtil {
    public static String mainHelp() {
        return "主菜单\n" +
                "====================\n" +
                "操作：\n" +
                "list    查看名下的会话信息\n" +
                "modify    修改名下会话信息\n" +
                "announce    向MC端发送公告\n" +
                "exit    返回到上一级（全局通用）\n" +
                "quit    退出指令";
    }

    public static String listHelp() {
        return "主菜单/查看名下会话信息\n" +
                "====================\n" +
                "操作：\n" +
                "<会话号>    发送会话号以查看信息\n" +
                "all    查看所有会话号\n" +
                "exit    返回到上一级\n" +
                "quit    退出指令";
    }

    public static String modifyHelp() {
        return "主菜单/修改名下会话\n" +
                "====================\n" +
                "操作：\n" +
                "<会话号>    需要修改的会话号\n" +
                "exit    退出修改会话操作\n" +
                "quit    退出指令";
    }

    public static String modifyMenuHelp() {
        return "主菜单/修改名下会话/选择修改操作\n" +
                "====================\n" +
                "操作：\n" +
                "gadd    添加互通群\n" +
                "gdel    删除互通群\n" +
                "name    修改会话名\n" +
                "format    修改群之间互通消息格式\n" +
                "ok    确认应用此操作\n" +
                "exit    退出修改该会话操作\n" +
                "quit    退出指令";
    }

    public static String modifyGroupAddHelp() {
        return "主菜单/修改名下会话/添加互通群\n" +
                "====================\n" +
                "一个例子：\n" +
                "123456789 互通群\n" +
                "====================\n" +
                "操作：\n" +
                "<群号> <群备注>    输入新群号与群昵称（群昵称为自定义，相当于给群起一个名）\n" +
                "exit    退出添加互通群操作\n" +
                "quit    退出指令";
    }

    public static String modifyGroupDelHelp() {
        return "主菜单/修改名下会话/删除互通群\n" +
                "====================\n" +
                "操作：\n" +
                "<群号>    需要删除的群号\n" +
                "exit    退出删除互通群操作\n" +
                "quit    退出指令";
    }

    public static String modifyFormatHelp() {
        return "主菜单/修改名下会话/修改互通消息格式\n" +
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
                "操作：\n" +
                "<消息格式>    新的群之间消息互通格式\n" +
                "default    使用默认消息格式\n" +
                "exit    退出修改互通格式操作\n" +
                "quit    退出指令";
    }

    public static String modifySessionNameHelp() {
        return "主菜单/修改名下会话/修改会话名\n" +
                "====================\n" +
                "操作：\n" +
                "<会话名>    新的会话名\n" +
                "exit    退出修改会话名\n" +
                "quit    退出指令";
    }


}
