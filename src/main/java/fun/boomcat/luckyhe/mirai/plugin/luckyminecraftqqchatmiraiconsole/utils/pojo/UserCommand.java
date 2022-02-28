package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.pojo;

import lombok.Data;

@Data
public class UserCommand {
    private long sessionId;
    private String sessionName;
    private String serverName;

    @Override
    public String toString() {
        return "会话号：" + sessionId + "\n" +
                "会话名：" + sessionName + "\n" +
                "连接：" + serverName;
    }
}
