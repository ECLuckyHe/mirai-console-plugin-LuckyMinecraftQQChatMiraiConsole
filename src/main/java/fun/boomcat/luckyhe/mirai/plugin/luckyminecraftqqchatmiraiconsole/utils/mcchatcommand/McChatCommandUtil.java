package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.mcchatcommand;

public class McChatCommandUtil {
    public static String mainHelp() {
        return "主菜单\n" +
                "====================\n" +
                "操作：\n" +
                "list    查看名下的会话信息\n" +
                "modify    修改名下会话信息\n" +
                "announce    向MC端发送公告\n" +
                "uc    用户指令相关设置\n" +
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
                "list    查看名下会话号列表\n" +
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
                "一个例子：\n" +
                "[%groupNickname%] <%senderGroupNickname%> %message%\n" +
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

    public static String announceHelp() {
        return "主菜单/发送公告\n" +
                "====================\n" +
                "操作：\n" +
                "content    指定公告内容\n" +
                "mc    指定需要发送的MC端\n" +
                "ok    确认发送\n" +
                "exit    退出发送公告\n" +
                "quit    退出指令";
    }

    public static String announceContentHelp() {
        return "主菜单/发送公告/指定公告内容\n" +
                "====================\n" +
                "操作：\n" +
                "<公告内容>    发送的内容作为公告内容\n" +
                "exit    退出指定公告内容\n" +
                "quit    退出指令";
    }

    public static String announceMcHelp() {
        return "主菜单/发送公告/指定MC端\n" +
                "====================\n" +
                "操作：\n" +
                "<会话号> <MC连接名>    选择或取消选择接收公告的连接名\n" +
                "<会话号>    选择或取消选择该会话的所有MC连接\n" +
                "all    向所有会话的MC连接发送公告\n" +
                "exit    退出指定MC端操作\n" +
                "quit    退出指令";
    }

    public static String userCommandHelp() {
        return "主菜单/用户指令设置\n" +
                "====================\n" +
                "操作：\n" +
                "<会话号> <连接名>    输入会话号和连接名以操作指定连接\n" +
                "list    列出所有会话名和连接名\n" +
                "exit    退出用户指令设置\n" +
                "quit    退出指令";
    }

    public static String userCommandMenuHelp() {
        return "主菜单/用户指令设置/选择修改操作\n" +
                "====================\n" +
                "由于用户指令存储在MC端\n" +
                "这里的操作都是在确认后才进行远程修改\n" +
                "====================\n" +
                "操作：\n" +
                "list    查看当前存在的用户指令\n" +
                "add    添加用户指令\n" +
                "del    删除用户指令\n" +
                "exit    退出用户指令设置\n" +
                "quit    退出指令";
    }

    public static String userCommandAddNameHelp() {
        return "主菜单/用户指令设置/添加用户指令/输入新用户指令名\n" +
                "====================\n" +
                "操作：\n" +
                "<指令名>    输入新的指令名用于标记该指令（备注）\n" +
                "exit    退出添加用户指令\n" +
                "quit    退出指令";
    }

    public static String userCommandAddCommandHelp() {
        return "主菜单/用户指令设置/添加用户指令/输入用户指令\n" +
                "====================\n" +
                "注意：此步要求输入的是用户输入的指令\n" +
                "假设用户指令为：创造\n" +
                "实际执行指令为：gamemode creative %playerName%\n" +
                "那么当Lucky_He在同会话群内发送：创造\n" +
                "则MC端执行指令：/gamemode creative Lucky_He\n" +
                "至于QQ与MC的ID的映射关系，由玩家自行绑定\n" +
                "====================\n" +
                "自定义参数格式为：#{test}\n" +
                "如定义用户指定：模式 #{mode}\n" +
                "实际指令：gamemode #{mode} %playerName%\n" +
                "则当Lucky_He在群内发送：模式 survival\n" +
                "则MC端执行指令：/gamemode survival Lucky_He\n" +
                "====================\n" +
                "实际指令中的实际玩家名字请用%playerName%代替，实际内容为该qq绑定的mcid\n" +
                "#{test} 的花括号中可以取任意名字，但不能包含空格\n" +
                "====================\n" +
                "操作：\n" +
                "<新指令>    一个新的用户指令\n" +
                "exit    退出添加用户指令\n" +
                "quit    退出指令";
    }
    public static String userCommandAddMappingHelp() {
        return "主菜单/用户指令设置/添加用户指令/输入实际指令\n" +
                "====================\n" +
                "该步输入的是用户指令对应的实际指令\n" +
                "当用户发送用户指令时，实际执行的是该条指令\n" +
                "实际指令中的实际玩家名字请用%playerName%代替，实际内容为该qq绑定的mcid\n" +
                "#{test} 类型的参数必须要在该步中全部使用\n" +
                "====================\n" +
                "操作：\n" +
                "<新指令>    输入用户指令对应的实际指令\n" +
                "exit    退出添加用户指令\n" +
                "quit    退出指令";
    }

    public static String userCommandAddConfirmHelp() {
        return "主菜单/用户指令设置/添加用户指令/确认添加\n" +
                "====================\n" +
                "ok    确认添加指令\n" +
                "exit    退出添加用户指令\n" +
                "quit    退出指令";
    }

    public static String userCommandDelHelp() {
        return "主菜单/用户指令设置/删除用户指令\n" +
                "====================\n" +
                "<指令名>    需要删除的用户指令名（再次发送为反选）\n" +
//                此处使用list查看指令列表，因此在添加指令名时应拒绝起名为list
                "list    查看当前存在的用户指令\n" +
                "ok    确认删除\n" +
                "exit    退出删除用户指令\n" +
                "quit    退出指令";
    }

}
