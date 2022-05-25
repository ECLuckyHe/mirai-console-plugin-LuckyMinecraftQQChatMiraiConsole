package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.handler.impl;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.SessionDataNotExistException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.handler.ResponseResult;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils.Result;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils.ResultCode;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.Session;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.SessionGroup;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.thread.MinecraftConnectionThread;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.SessionUtil;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionGetResponseResult implements ResponseResult {
    @Override
    public Result<?> handle(Map<String, Object> jsonMap) {
        long id;
        try {
            Object idObject = jsonMap.get("id");
            if (idObject == null) {
                return Result.error(ResultCode.SESSION_ID_NOT_PROVIDED);
            }
            id = idObject instanceof Integer ? (Integer) idObject : (Long) idObject;
        } catch (ClassCastException e) {
            return Result.error(ResultCode.SESSION_ID_TYPE_NOT_CORRECT);
        }

        Session session;
        try {
            session = SessionUtil.getSession(id);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Result.error(ResultCode.INNER_ERROR);
        } catch (SessionDataNotExistException e) {
            return Result.error(ResultCode.SESSION_NOT_EXISTED);
        }

        Map<String, Object> sessionMap = new HashMap<>();
        sessionMap.put("id", session.getId());
        sessionMap.put("name", session.getName());
        sessionMap.put("format", session.getFormatString());

        List<SessionGroup> groups = session.getGroups();
        List<Object> groupList = new ArrayList<>(groups.size());
        for (SessionGroup group : groups) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", group.getId());
            map.put("nickname", group.getName());
            groupList.add(map);
        }
        sessionMap.put("groups", groupList);

        sessionMap.put("administrator", session.getAdministrators());

        List<MinecraftConnectionThread> minecraftThreads = session.getMinecraftThreads();
        List<Object> mcConnections = new ArrayList<>(minecraftThreads.size());
        for (MinecraftConnectionThread thread : minecraftThreads) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", thread.getServerName().getContent());
            map.put("address", thread.getServerAddress());
            map.put("joinFormat", thread.getJoinFormatString().getContent());
            map.put("quitFormat", thread.getQuitFormatString().getContent());
            map.put("msgFormat", thread.getMsgFormatString().getContent());
            map.put("deathFormat", thread.getDeathFormatString().getContent());
            map.put("kickFormat", thread.getKickFormatString().getContent());
            mcConnections.add(map);
        }
        sessionMap.put("mcConnections", mcConnections);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("session", sessionMap);
        return Result.success(resultMap);
    }
}
