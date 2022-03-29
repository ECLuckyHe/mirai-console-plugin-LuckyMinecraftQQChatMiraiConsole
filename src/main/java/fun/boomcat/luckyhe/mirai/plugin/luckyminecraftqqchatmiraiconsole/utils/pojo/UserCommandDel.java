package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.pojo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserCommandDel {
    private long sessionId;
    private String sessionName = null;
    private String serverName = null;
    private List<String> delNames = new ArrayList<>();

    public void selectDelName(String commandName) {
        if (delNames.contains(commandName)) {
            delNames.removeIf(o -> o.equals(commandName));
        } else {
            delNames.add(commandName);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("会话号：" + sessionId + "\n" +
                "会话名：" + sessionName + "\n" +
                "连接名：" + serverName + "\n" +
                "即将删除的指令名：");
        int size = delNames.size();
        if (size == 0) {
            return sb.append("无").toString();
        }

        sb.append("\n");
        for (String s : delNames) {
            sb.append("    ").append(s).append("\n");
        }
        return sb.toString();
    }
}
