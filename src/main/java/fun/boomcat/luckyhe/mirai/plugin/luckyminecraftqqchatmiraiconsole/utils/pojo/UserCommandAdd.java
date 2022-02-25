package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.pojo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class UserCommandAdd {
    private long sessionId;
    private String sessionName = null;
    private String serverName = null;
    private String name = null;
    private String command = null;
    private String mapping = null;

    public List<String> getCommandArgList() {
        Matcher matcher = Pattern.compile("(^|(?<= ))#\\{\\S+}((?= )|$)").matcher(command);
        List<String> temp = new ArrayList<>();

        while (matcher.find()) {
            temp.add(matcher.group());
        }

//        去重
        List<String> res = new ArrayList<>();
        for (String s : temp) {
            if (!res.contains(s)) {
                res.add(s);
            }
        }

        return res;
    }

    public List<String> splitCommand() {
//        分割command
        List<String> res = new ArrayList<>();
        String temp = new String(command.trim());
        while (!temp.equals("")) {
            String[] split = temp.split(" ");
            res.add(split[0]);
            temp = temp.substring(split[0].length()).trim();
        }
        return res;
    }

    public void setCommand(String command) {
        this.command = command;
        List<String> strings = splitCommand();
        StringBuilder sb = new StringBuilder();
        for (String string : strings) {
            sb.append(string).append(" ");
        }
        this.command = sb.toString().trim();
    }

    @Override
    public String toString() {
        return "会话号：" + sessionId + "\n" +
                "会话名：" + sessionName + "\n" +
                "指令名：" + (name == null ? "未指定" : name) + "\n" +
                "用户指令：" + (command == null ? "未指定" : command) + "\n" +
                "实际指令：" + (mapping == null ? "未指定" : mapping);
    }
}
