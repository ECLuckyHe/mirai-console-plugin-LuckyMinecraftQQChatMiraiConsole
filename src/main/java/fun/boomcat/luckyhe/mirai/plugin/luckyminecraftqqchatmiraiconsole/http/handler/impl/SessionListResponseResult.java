package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.handler.impl;

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

public class SessionListResponseResult implements ResponseResult {
    @Override
    public Result<?> handle(Map<String, Object> jsonMap) {
        int pageSize;
        try {
            Object pageSizeObject = jsonMap.get("pageSize");
            if (pageSizeObject == null) {
                return Result.error(ResultCode.PAGE_SIZE_NOT_PROVIDED);
            }
            pageSize = (Integer) pageSizeObject;
        } catch (ClassCastException e) {
            return Result.error(ResultCode.PAGE_SIZE_TYPE_NOT_CORRECT);
        }

        int pageNo;
        try {
            Object pageNoObject = jsonMap.get("pageNo");
            if (pageNoObject == null) {
                return Result.error(ResultCode.PAGE_NO_NOT_PROVIDED);
            }
            pageNo = (Integer) pageNoObject;
        } catch (ClassCastException e) {
            return Result.error(ResultCode.PAGE_NO_TYPE_NOT_CORRECT);
        }

        if (pageSize <= 0) {
            return Result.error(ResultCode.PAGE_SIZE_VALUE_NOT_CORRECT);
        }
        if (pageNo <= 0) {
            return Result.error(ResultCode.PAGE_NO_VALUE_NOT_CORRECT);
        }

        int start = pageSize * (pageNo - 1);
        int endExclude = start + pageSize;

        List<Session> sessions;
        try {
            sessions = SessionUtil.getSessions();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Result.error(ResultCode.INNER_ERROR);
        }

        Map<String, Object> resultMap = new HashMap<>();

        List<Object> resultSessionList = new ArrayList<>(pageSize);
        for (int i = start; i < sessions.size() && i < endExclude; i++) {
            Session session = sessions.get(i);
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

            sessionMap.put("administrators", session.getAdministrators());

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

            resultSessionList.add(sessionMap);
        }

        resultMap.put("sessions", resultSessionList);
        return Result.success(resultMap);
    }
}
