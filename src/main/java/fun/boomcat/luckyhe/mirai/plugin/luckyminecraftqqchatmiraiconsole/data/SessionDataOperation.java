package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.data;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.LuckyMinecraftQQChatMiraiConsole;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.SessionDataExistException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.SessionDataGroupExistException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.SessionDataGroupNotExistException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.SessionDataNotExistException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.Session;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.SessionGroup;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.SessionUtil;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionDataOperation {
    private static File dataPath;
    private static String sessionDataFilename = "sessionData.yml";
    private static Yaml yaml = new Yaml();
    private static List<Object> sessionDataList;
    private static LuckyMinecraftQQChatMiraiConsole INSTANCE;

    public static void initSessionDataPath(File path, String sessionDataContent, LuckyMinecraftQQChatMiraiConsole I) throws IOException {
        INSTANCE = I;
        dataPath = path;
        File[] files = dataPath.listFiles();
        boolean hasSessionData = false;
        for (File file : files) {
            if (file.getName().equals(sessionDataFilename)) {
                hasSessionData = true;
                break;
            }
        }

        if (!hasSessionData) {
            copySessionDataFromResource(sessionDataContent);
        }
    }

    private static void copySessionDataFromResource(String sessionDataContent) throws IOException {
        FileOutputStream fos = new FileOutputStream(dataPath.getPath() + "/" + sessionDataFilename);

        byte[] buf = sessionDataContent.getBytes(StandardCharsets.UTF_8);
        fos.write(buf);
        fos.close();
    }

    public static List<Object> getSessionDataList() throws FileNotFoundException {
        if (sessionDataList == null) {
            sessionDataList = yaml.load(new InputStreamReader(new FileInputStream(dataPath.getPath() + "/" + sessionDataFilename), StandardCharsets.UTF_8));
        }

        return sessionDataList;
    }

    private static void writeFile() throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(dataPath.getPath() + "/" + sessionDataFilename), StandardCharsets.UTF_8);
        yaml.dump(getSessionDataList(), osw);
        osw.close();

        sessionDataList = null;

//        停止主线程
        INSTANCE.stopServerMainThread();

//        清除所有Session对象
        SessionUtil.clear();

//        重新开启线程
        INSTANCE.newServerMainThread();
        INSTANCE.getServerMainThread().start();
    }

    public static Map<String, Object> getSessionData(long sessionId) throws FileNotFoundException, SessionDataNotExistException {
        List<Object> sessionDataList = getSessionDataList();
        for (Object obj : sessionDataList) {
            Map<String, Object> map = (Map<String, Object>) obj;
            Object readSessionId = map.get("id");

            long rak;
            if (readSessionId instanceof Integer) {
                rak = ((int) readSessionId);
            } else {
                rak = ((long) readSessionId);
            }

            if (rak == sessionId) {
                return map;
            }
        }

        throw new SessionDataNotExistException();
    }

    public static void addSessionData(long sessionId, String sessionName, String formatString) throws IOException, SessionDataExistException {
        try {
            getSessionData(sessionId);
            throw new SessionDataExistException();
        } catch (SessionDataNotExistException ignored) {

        }

        Map<String, Object> newMap = new HashMap<>();
        newMap.put("id", sessionId);
        newMap.put("name", sessionName);
        newMap.put("groups", new ArrayList<Long>());
        newMap.put("format", formatString);

        getSessionDataList().add(newMap);
        writeFile();
    }

    public static void addSessionDataGroup(long sessionId, long newGroupId, String groupNickname) throws IOException, SessionDataNotExistException, SessionDataGroupExistException {
        Map<String, Object> sessionData = getSessionData(sessionId);

        List<Object> groupList = (List<Object>) sessionData.get("groups");
        for (Object group : groupList) {
            Object groupSourceId = ((Map<String, Object>) group).get("id");
            long groupId;
            if (groupSourceId instanceof Integer) {
                groupId = ((int) groupSourceId);
            } else {
                groupId = ((long) groupSourceId);
            }

            if (groupId == newGroupId) {
                throw new SessionDataGroupExistException();
            }
        }

        Map<String, Object> newGroupMap = new HashMap<>();
        newGroupMap.put("id", newGroupId);
        newGroupMap.put("nickname", groupNickname);
        groupList.add(newGroupMap);

        writeFile();
    }

    public static void removeSessionDataGroup(long sessionId, long oldGroupId) throws IOException, SessionDataNotExistException, SessionDataGroupNotExistException {
        Map<String, Object> sessionData = getSessionData(sessionId);

        List<Object> groupList = (List<Object>) sessionData.get("groups");
        for (Object group : groupList) {
            Object groupSourceId = ((Map<String, Object>) group).get("id");
            long groupId;
            if (groupSourceId instanceof Integer) {
                groupId = (int) groupSourceId;
            } else {
                groupId = (long) groupSourceId;
            }

            if (groupId == oldGroupId) {
                groupList.remove(group);
                writeFile();
                return;
            }
        }

        throw new SessionDataGroupNotExistException();
    }

    public static void modifySessionDataName(long sessionId, String newSessionName) throws SessionDataNotExistException, IOException {
        Map<String, Object> sessionData = getSessionData(sessionId);
        sessionData.put("name", newSessionName);
        writeFile();
    }

    public static void modifySessionDataFormat(long sessionId, String newFormatString) throws SessionDataNotExistException, IOException {
        Map<String, Object> sessionData = getSessionData(sessionId);
        sessionData.put("format", newFormatString);
        writeFile();
    }

    public static void removeSessionData(long sessionId) throws IOException, SessionDataNotExistException {
        Map<String, Object> map = getSessionData(sessionId);

        getSessionDataList().remove(map);
        writeFile();
    }

    public static List<Session> getSessionObjects() throws FileNotFoundException {
        List<Object> sessionDataList = getSessionDataList();
        List<Session> sessionObjects = new ArrayList<>();

        for (Object sessionData : sessionDataList) {
            Map<String, Object> sessionMap = (Map<String, Object>) sessionData;
            long sessionId = sessionMap.get("id") instanceof Integer ? (int) sessionMap.get("id") : (long) sessionMap.get("id");
            String sessionName = ((String) sessionMap.get("name"));
            String formatString = ((String) sessionMap.get("format"));
            List<Object> groups = (List<Object>) sessionMap.get("groups");

            List<SessionGroup> groupObjects = new ArrayList<>();
            for (Object group : groups) {
                Map<String, Object> groupMap = (Map<String, Object>) group;
                long groupId = groupMap.get("id") instanceof Integer ? (int) groupMap.get("id") : (long) groupMap.get("id");
                String groupNickname = (String) groupMap.get("nickname");

                groupObjects.add(new SessionGroup(groupId, groupNickname));
            }

            sessionObjects.add(new Session(sessionId, sessionName, groupObjects, formatString));
        }

        return sessionObjects;
    }
}
