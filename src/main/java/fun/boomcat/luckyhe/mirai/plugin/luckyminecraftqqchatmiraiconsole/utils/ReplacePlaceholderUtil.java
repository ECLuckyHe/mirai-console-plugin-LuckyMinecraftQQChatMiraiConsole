package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils;

import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplacePlaceholderUtil {
    @Deprecated
    @SuppressWarnings("过时的方法，请使用同类的replacePlaceholderWithString方法")
    public static String groupMessageReplace(
            String sessionName,
            String formatString,
            long groupId,
            String groupName,
            String groupNickname,
            long senderId,
            String senderNickname,
            String senderGroupNickname,
            String message
    ) {
        return formatString.replaceAll(QqFormatPlaceholder.GROUP_ID, String.valueOf(groupId))
                .replaceAll(QqFormatPlaceholder.GROUP_NAME, groupName)
                .replaceAll(QqFormatPlaceholder.GROUP_NICKNAME, groupNickname)
                .replaceAll(QqFormatPlaceholder.SENDER_ID, String.valueOf(senderId))
                .replaceAll(QqFormatPlaceholder.SENDER_NICKNAME, senderNickname)
                .replaceAll(QqFormatPlaceholder.SENDER_GROUP_NICKNAME, senderGroupNickname.length() == 0 ? senderNickname : senderGroupNickname)
                .replaceAll(QqFormatPlaceholder.SESSION_NAME, sessionName)
                .replaceAll(QqFormatPlaceholder.MESSAGE, message);
    }

    public static String replacePlaceholderWithString(String formatString, String ...strings) {
//        奇数位置为占位符，偶数位置为实际内容
        if (strings.length % 2 != 0) {
            return null;
        }

        StringBuilder patternString = new StringBuilder();

//        奇数位置整合成以下格式：aaa|bbb|ccc|ddd
        for (int i = 0; i < strings.length; i += 2) {
            patternString.append(strings[i]);
            if (i + 2 < strings.length) {
                patternString.append("|");
            }
        }

        Pattern p = Pattern.compile(patternString.toString());
        Matcher m = p.matcher(formatString);

        StringBuffer sb = new StringBuffer();

//        偶数位置，用实际内容替换掉占位符
        while (m.find()) {
            for (int i = 1; i < strings.length; i += 2) {
                if (m.group().equals(strings[i - 1])) {
                    m.appendReplacement(sb, strings[i]);
                }
            }
        }
        m.appendTail(sb);

        return sb.toString();
    }

    public static MessageChain groupMessageReplace(
            String sessionName,
            String formatString,
            long groupId,
            String groupName,
            String groupNickname,
            long senderId,
            String senderNickname,
            String senderGroupNickname,
            MessageChain message
    ) {
//        来自bot的群消息替换
        String tempRes = formatString.replaceAll(QqFormatPlaceholder.GROUP_ID, String.valueOf(groupId))
                .replaceAll(QqFormatPlaceholder.GROUP_NAME, groupName)
                .replaceAll(QqFormatPlaceholder.GROUP_NICKNAME, groupNickname)
                .replaceAll(QqFormatPlaceholder.SENDER_ID, String.valueOf(senderId))
                .replaceAll(QqFormatPlaceholder.SENDER_NICKNAME, senderNickname)
                .replaceAll(QqFormatPlaceholder.SENDER_GROUP_NICKNAME, senderGroupNickname.length() == 0 ? senderNickname : senderGroupNickname)
                .replaceAll(QqFormatPlaceholder.SESSION_NAME, sessionName);

        String[] splitTempRes = tempRes.split(QqFormatPlaceholder.MESSAGE, -1);
        MessageChainBuilder mcb = new MessageChainBuilder();
        if (splitTempRes.length <= 1) {
            mcb.append(tempRes);
        } else {
            for (int i = 0; i < splitTempRes.length; i++) {
                mcb.append(splitTempRes[i]);
                if (!(i == splitTempRes.length - 1)) {
                    mcb.append(message);
                }
            }
        }

        return mcb.build();
    }
}
