package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.data.SessionDataOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.SessionDataNotExistException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.Session;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.utils.MiraiLogger;

import java.io.FileNotFoundException;
import java.util.List;

public class SessionUtil {
    private static List<Session> sessions;
    private static MiraiLogger logger = MiraiLoggerUtil.getLogger();

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
            AsyncCaller.run(() -> {
                try {
                    session.sendMessageFromGroup(bot, groupId, groupName, senderId, senderNickname, senderGroupNickname, message);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void closeAllConnections(String info) throws FileNotFoundException {
        logger.info("开始关闭所有连接线程");
        for (Session session : getSessions()) {
            session.sendClosePacketToMinecraftThread(info);
            while (session.getMinecraftThreads().size() != 0) {
            }
        }
        logger.info("所有连接线程关闭完成");
    }
}
