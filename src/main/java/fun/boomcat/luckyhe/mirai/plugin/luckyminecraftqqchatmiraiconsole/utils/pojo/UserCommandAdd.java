package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.pojo;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.UserCommandUtil;
import lombok.Data;

import java.util.List;

@Data
public class UserCommandAdd {
    private long sessionId;
    private String sessionName = null;
    private String serverName = null;
    private String name = null;
    private String command = null;
    private String mapping = null;

    public List<String> getCommandArgList() {
        return UserCommandUtil.getCommandArgList(command);
    }

    public List<String> getMappingArgList() {
        return UserCommandUtil.getCommandArgList(mapping);
    }

    public void setCommand(String command) {
        this.command = UserCommandUtil.craftCommand(UserCommandUtil.splitCommand(command));
    }

    public void setMapping(String mapping) {
        if (mapping == null) {
            this.mapping = null;
        } else {
            this.mapping = UserCommandUtil.craftCommand(UserCommandUtil.splitCommand(mapping));
        }
    }

    @Override
    public String toString() {
        return "会话号：" + sessionId + "\n" +
                "会话名：" + sessionName + "\n" +
                "连接名：" + serverName + "\n" +
                "指令名：" + (name == null ? "未指定" : name) + "\n" +
                "用户指令：" + (command == null ? "未指定" : command) + "\n" +
                "实际指令：" + (mapping == null ? "未指定" : mapping);
    }
}
