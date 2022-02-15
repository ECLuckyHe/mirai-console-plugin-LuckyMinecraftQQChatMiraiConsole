package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils;

import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.contact.Contact;

public class MessageUtil {
    public static void pageSender(Contact contact, String message) {
        int splitLength = 2000;
        int strLen = message.length();
        int totalPages = strLen / splitLength + (strLen % splitLength == 0 ? 0 : 1);

        StringBuilder sb = new StringBuilder(message);

        int current = 1;
        while (sb.length() != 0) {
            String part;
            try {
                part = sb.substring(0, splitLength);
                sb = new StringBuilder(sb.substring(splitLength, sb.length()));
            } catch (IndexOutOfBoundsException e) {
                part = sb.substring(0, sb.length());
                sb = new StringBuilder();
            }

            contact.sendMessage("<第" + current + "页，共" + totalPages + "页>\n" + part);

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
                contact.sendMessage("发生了其它错误，请联系开发者");
            }

            current ++;
        }
    }
}
