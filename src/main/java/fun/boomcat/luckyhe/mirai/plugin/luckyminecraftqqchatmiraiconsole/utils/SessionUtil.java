package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.data.SessionDataOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.SessionDataNotExistException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.Session;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.SessionGroup;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.thread.MinecraftConnectionThread;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.utils.MiraiLogger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class SessionUtil {
    private static List<Session> sessions;
    private static final MiraiLogger logger = MiraiLoggerUtil.getLogger();

    public static List<Session> getSessions() throws IOException {
        if (sessions == null) {
            sessions = SessionDataOperation.getSessionObjects();
        }

        return sessions;
    }

    public static void clear() {
        sessions = null;
    }

    public static Session getSession(long sessionId) throws IOException, SessionDataNotExistException {
        List<Session> sessions = getSessions();
        for (Session session : sessions) {
            if (session.getId() == sessionId) {
                return session;
            }
        }

        throw new SessionDataNotExistException();
    }

    public static Session getUserSession(long sessionId, long qq) throws IOException, SessionDataNotExistException {
        List<Session> userSessions = getUserSessions(qq);
        for (Session session : userSessions) {
            if (session.getId() == sessionId) {
                return session;
            }
        }

        throw new SessionDataNotExistException();
    }

    public static String sessionToString(Session session) {
        StringBuilder sb = new StringBuilder();
        sb.append("会话号：").append(session.getId()).append("\n");
        sb.append("会话名：").append(session.getName()).append("\n");
        sb.append("群间消息格式：").append(session.getFormatString()).append("\n");
        sb.append("群列表：");

        List<SessionGroup> groups = session.getGroups();
        if (groups.size() == 0) {
            sb.append("空\n");
        } else {
            sb.append("\n");
            for (SessionGroup group : groups) {
                sb.append("    ").append(group.getName()).append("(").append(group.getId()).append(")").append("\n");
            }
        }

        sb.append("管理员列表：");
        List<Long> administrators = session.getAdministrators();
        if (administrators.size() == 0) {
            sb.append("空\n");
        } else {
            sb.append("\n");
            for (Long administrator : administrators) {
                sb.append("    ").append(administrator).append("\n");
            }
        }

        sb.append("连接：");
        List<MinecraftConnectionThread> threads = session.getMinecraftThreads();
        if (threads.size() == 0) {
            sb.append("无\n");
        } else {
            sb.append("\n");
            for (MinecraftConnectionThread thread : threads) {
                sb.append("    ").append(thread.getServerName().getContent()).append("(").append(thread.getServerAddress()).append(")").append("\n");
            }
        }

        return sb.toString();
    }

    public static void sendMessageFromGroup(
            Bot bot,
            Group group,
            Member member,
            MessageChain message
    ) throws IOException {
        List<Session> sessions = getSessions();
        for (Session session : sessions) {
            AsyncCaller.run(() -> {
                try {
                    session.sendMessageFromGroup(bot, group, member, message);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void closeAllConnections(String info) throws IOException, InterruptedException {
        logger.info("开始关闭所有连接线程");
        CountDownLatch cdl = new CountDownLatch(getSessions().size());
        for (Session session : getSessions()) {
            AsyncCaller.run(() -> {
                session.sendClosePacketToMinecraftThreads(info);
                while (session.getMinecraftThreads().size() != 0) {
                    logger.info("等待会话" + session.getId() + "关闭所有连接，当前剩余" + session.getMinecraftThreads().size() + "个连接");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                cdl.countDown();
            });
        }

        cdl.await();
        logger.info("所有连接线程关闭完成");
    }

    public static Session copySessionWithNoThreads(Session session) {
//        复制一个Session对象
        List<SessionGroup> sessionGroups = new ArrayList<>();
        for (SessionGroup group : session.getGroups()) {
            sessionGroups.add(new SessionGroup(group.getId(), group.getName()));
        }

        List<Long> administrators = new ArrayList<>(session.getAdministrators());

        return new Session(session.getId(), session.getName(), sessionGroups, session.getFormatString(), administrators);
    }

    public static List<Session> getUserSessions(Long qq) throws IOException {
        List<Session> res = new ArrayList<>();

        List<Session> sessions = getSessions();
        for (Session session : sessions) {
            if (session.getAdministrators().contains(qq)) {
                res.add(session);
            }
        }

        return res;
    }
}
