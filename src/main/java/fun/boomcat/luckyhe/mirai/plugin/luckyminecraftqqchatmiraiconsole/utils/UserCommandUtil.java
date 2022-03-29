package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.thread.MinecraftConnectionThread;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.ForwardMessage;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.PlainText;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserCommandUtil {
    public static List<String> splitCommand(String command) {
        List<String> res = new ArrayList<>();
        while (!command.equals("")) {
            String[] split = command.split("\\s+");
            res.add(split[0]);
            command = command.substring(split[0].length()).trim();
        }
        return res;
    }

    public static String craftCommand(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s).append(" ");
        }
        return sb.toString().trim();
    }

    public static List<String> getCommandArgList(String command) {
        List<String> strings = splitCommand(command);
        List<String> temp = new ArrayList<>();
        for (String s : strings) {
            if (s.matches("#\\{\\S+}")) {
                temp.add(s);
            }
        }

        List<String> res = new ArrayList<>();
        for (String s : temp) {
            if (!res.contains(s)) {
                res.add(s);
            }
        }
        return res;
    }

    public static ForwardMessage getForwardMessage(Bot bot, Contact contact, List<Map<String, String>> commandMaps, MinecraftConnectionThread thread) {
//        当为普通用户获取用户指令的时候，传入thread的值为null
        List<StringBuilder> sbs = new ArrayList<>();
        int count = 0;
        int PER_MSG_COUNT = 30;
        int totalPages = commandMaps.size() % PER_MSG_COUNT == 0 ?
                commandMaps.size() / PER_MSG_COUNT :
                commandMaps.size() / PER_MSG_COUNT + 1;
        for (Map<String, String> map : commandMaps) {
            int page = count / PER_MSG_COUNT;
            StringBuilder stringBuilder;

            try {
                stringBuilder = sbs.get(page);
            } catch (IndexOutOfBoundsException e) {
                stringBuilder = new StringBuilder("第" + (page + 1) + "页，共" + totalPages + "页\n" +
                        "==========\n");
                sbs.add(stringBuilder);
            }

            stringBuilder.append("指令名：").append(map.get("name")).append("\n");
            stringBuilder.append("用户指令：").append(map.get("command")).append("\n");
            stringBuilder.append("实际指令：").append(map.get("mapping")).append("\n");
            stringBuilder.append("\n");

            count += 1;
        }

        ForwardMessageBuilder fmb = new ForwardMessageBuilder(contact);
        if (thread != null) {
            fmb.add(bot.getId(), bot.getNick(), new PlainText("服务端名称：" + thread.getServerName().getContent() + "\n" +
                    "指令前缀：" + ReplacePlaceholderUtil.replacePlaceholderWithString(
                    thread.getUserCommandPrefix().getContent(),
                    MinecraftFormatPlaceholder.SERVER_NAME,
                    thread.getServerName().getContent()
            )));
        }
        for (StringBuilder sb : sbs) {
            fmb.add(bot.getId(), bot.getNick(), new PlainText(sb.toString()));
        }

        return fmb.build();
    }
}
