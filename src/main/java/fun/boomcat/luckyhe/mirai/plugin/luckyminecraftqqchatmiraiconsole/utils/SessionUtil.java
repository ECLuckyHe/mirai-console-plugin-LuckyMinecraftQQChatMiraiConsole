package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.data.SessionDataOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.SessionDataNotExistException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.Session;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.SessionGroup;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.MessageChain;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.NoSuchElementException;

public class SessionUtil {
    private static List<Session> sessions;

    public static List<Session> getSessions() throws FileNotFoundException {
        if (sessions == null) {
            sessions = SessionDataOperation.getSessionObjects();
        }

        return sessions;
    }

    public static void clear() {
        sessions = null;
    }

    public static Session getSession(long sessionId) throws FileNotFoundException, SessionDataNotExistException {
        List<Session> sessions = getSessions();
        for (Session session : sessions) {
            if (session.getId() == sessionId) {
                return session;
            }
        }

        throw new SessionDataNotExistException();
    }

    public static void sendMessageFromGroup(
            Bot bot,
            long groupId,
            String groupName,
            long senderId,
            String senderNickname,
            String senderGroupNickname,
            MessageChain message
    ) throws FileNotFoundException {
        List<Session> sessions = getSessions();
        for (Session session : sessions) {
            boolean hasGroup = false;
            String groupNickname = null;
            for (SessionGroup sessionGroup : session.getGroups()) {
                if (sessionGroup.getId() == groupId) {
                    hasGroup = true;
                    groupNickname = sessionGroup.getName();
                    break;
                }
            }

            if (!hasGroup) {
                continue;
            } else {
                MessageChain messageToBeSent = ReplacePlaceholderUtil.groupMessageReplace(
                        session.getName(),
                        session.getFormatString(),
                        groupId,
                        groupName,
                        groupNickname,
                        senderId,
                        senderNickname,
                        senderGroupNickname,
                        message
                );

                for (SessionGroup sessionGroup : session.getGroups()) {
                    if (sessionGroup.getId() != groupId) {
                        try {
                            bot.getGroupOrFail(sessionGroup.getId()).sendMessage(messageToBeSent);
                        } catch (NoSuchElementException ignored) {

                        }
                    }
                }
            }
        }
    }
}
