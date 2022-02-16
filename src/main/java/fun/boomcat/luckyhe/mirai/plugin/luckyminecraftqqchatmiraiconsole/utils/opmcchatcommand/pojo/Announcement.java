package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.opmcchatcommand.pojo;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.Session;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.thread.MinecraftConnectionThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Announcement {
    private String content;
    private Map<Session, List<String>> minecraftThreadsMap = new HashMap<>();

    public Announcement() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<Session, List<String>> getThreadsMap() {
        return minecraftThreadsMap;
    }

    public void setThreadsMap(Map<Session, List<String>> threadsMap) {
        this.minecraftThreadsMap = threadsMap;
    }

    public void addMinecraftThread(Session session, String threadName) {
//        添加连接名
        List<String> threadNames = minecraftThreadsMap.computeIfAbsent(session, k -> new ArrayList<>());
        if (!threadNames.contains(threadName)) {
            threadNames.add(threadName);
        }
    }

    public void delMinecraftThread(Session session, String threadName) {
        List<String> threadNames = minecraftThreadsMap.computeIfAbsent(session, k -> new ArrayList<>());
        threadNames.removeIf(o -> o.equals(threadName));
    }

    public boolean isMinecraftThreadExist(Session session, String threadName) {
        List<String> threadNames = minecraftThreadsMap.computeIfAbsent(session, k -> new ArrayList<>());
        return threadNames.contains(threadName);
    }

    public void clearMinecraftThreads(Session session) {
//        清除单个会话的所有连接
        List<String> threadNames = minecraftThreadsMap.computeIfAbsent(session, k -> new ArrayList<>());
        threadNames.clear();
    }

    public void clearAllMinecraftThreads(List<Session> sessions) {
//        清除所有连接
        for (Session session : sessions) {
            clearMinecraftThreads(session);
        }
    }

    public void selectAllMinecraftThreadsOfOneSession(Session session) {
//        选择单个会话的某个连接
        List<MinecraftConnectionThread> minecraftThreads = session.getMinecraftThreads();
        for (MinecraftConnectionThread thread : minecraftThreads) {
            addMinecraftThread(session, thread.getServerName().getContent());
        }
    }

    public void selectAllMinecraftThreadsOfAllSession(List<Session> sessions) {
//        选择所有会话的所有连接
        for (Session session : sessions) {
            selectAllMinecraftThreadsOfOneSession(session);
        }
    }

    public String toOutputString(List<Session> sessions) {
        StringBuilder sb = new StringBuilder();
        sb.append("公告内容：").append(content == null ? "未指定" : "\n" + content).append("\n");
        sb.append("已选对象（● 已选择  ○ 未选择  × 不存在的连接）：\n");

        for (Session session : sessions) {
            sb.append("    会话 ").append(session.getId()).append("(").append(session.getName()).append(")：").append("\n");

            List<MinecraftConnectionThread> minecraftThreads = session.getMinecraftThreads();
            List<String> markedThreadNames = new ArrayList<>();
            for (MinecraftConnectionThread thread : minecraftThreads) {
//                标记选择和未选择
                sb.append("    ").append("    ");
                if (minecraftThreadsMap.computeIfAbsent(session, k -> new ArrayList<>()).contains(thread.getServerName().getContent())) {
                    sb.append("● ");
                } else {
                    sb.append("○ ");
                }
                sb.append(thread.getServerName().getContent())
                        .append("(").append(thread.getServerAddress()).append(")").append("\n");
                markedThreadNames.add(thread.getServerName().getContent());
            }

//            遍历筛选出不存在的连接
            for (String threadName : minecraftThreadsMap.computeIfAbsent(session, k -> new ArrayList<>())) {
                if (!markedThreadNames.contains(threadName)) {
                    sb.append("    ").append("    ").append("× ").append(threadName).append("\n");
                }
            }
        }

        return sb.toString();
    }
}
