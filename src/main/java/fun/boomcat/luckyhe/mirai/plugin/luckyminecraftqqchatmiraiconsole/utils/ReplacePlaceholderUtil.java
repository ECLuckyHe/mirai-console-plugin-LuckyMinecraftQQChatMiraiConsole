package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils;

import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

public class ReplacePlaceholderUtil {
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
        return formatString.replaceAll(FormatPlaceholder.GROUP_ID, String.valueOf(groupId))
                .replaceAll(FormatPlaceholder.GROUP_NAME, groupName)
                .replaceAll(FormatPlaceholder.GROUP_NICKNAME, groupNickname)
                .replaceAll(FormatPlaceholder.SENDER_ID, String.valueOf(senderId))
                .replaceAll(FormatPlaceholder.SENDER_NICKNAME, senderNickname)
                .replaceAll(FormatPlaceholder.SENDER_GROUP_NICKNAME, senderGroupNickname.length() == 0 ? senderNickname : senderGroupNickname)
                .replaceAll(FormatPlaceholder.SESSION_NAME, sessionName)
                .replaceAll(FormatPlaceholder.MESSAGE, message);
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
        String tempRes = formatString.replaceAll(FormatPlaceholder.GROUP_ID, String.valueOf(groupId))
                .replaceAll(FormatPlaceholder.GROUP_NAME, groupName)
                .replaceAll(FormatPlaceholder.GROUP_NICKNAME, groupNickname)
                .replaceAll(FormatPlaceholder.SENDER_ID, String.valueOf(senderId))
                .replaceAll(FormatPlaceholder.SENDER_NICKNAME, senderNickname)
                .replaceAll(FormatPlaceholder.SENDER_GROUP_NICKNAME, senderGroupNickname.length() == 0 ? senderNickname : senderGroupNickname)
                .replaceAll(FormatPlaceholder.SESSION_NAME, sessionName);

        String[] splitTempRes = tempRes.split(FormatPlaceholder.MESSAGE, -1);
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
