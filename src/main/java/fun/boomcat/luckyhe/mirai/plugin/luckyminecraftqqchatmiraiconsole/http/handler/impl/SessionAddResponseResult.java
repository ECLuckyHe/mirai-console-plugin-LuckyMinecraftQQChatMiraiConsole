package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.handler.impl;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.data.SessionDataOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.SessionDataAdministratorExistException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.SessionDataExistException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.SessionDataGroupExistException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.SessionDataNotExistException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.handler.ResponseResult;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils.Result;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils.ResultCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SessionAddResponseResult implements ResponseResult {
    @Override
    public Result<?> handle(Map<String, Object> jsonMap) {
        Map<String, Object> requestSessionMap;
        try {
            Object requestSessionMapObject = jsonMap.get("session");
            if (requestSessionMapObject == null) {
                return Result.error(ResultCode.WRONG_REQUEST_DATA);
            }
            requestSessionMap = (Map<String, Object>) requestSessionMapObject;
        } catch (ClassCastException e) {
            return Result.error(ResultCode.WRONG_REQUEST_DATA);
        }

        long newId;
        try {
            Object newIdObject = requestSessionMap.get("id");
            if (newIdObject == null) {
                return Result.error(ResultCode.WRONG_REQUEST_DATA);
            }
            newId = newIdObject instanceof Integer ? (Integer) newIdObject : (Long) newIdObject;
        } catch (ClassCastException e) {
            return Result.error(ResultCode.WRONG_REQUEST_DATA);
        }
        System.out.println("newId = " + newId);

        String newName;
        try {
            Object newNameObject = requestSessionMap.get("name");
            if (newNameObject == null) {
                return Result.error(ResultCode.WRONG_REQUEST_DATA);
            }
            newName = (String) newNameObject;
        } catch (ClassCastException e) {
            return Result.error(ResultCode.WRONG_REQUEST_DATA);
        }
        System.out.println("newName = " + newName);

        String newFormat;
        try {
            Object newFormatObject = requestSessionMap.get("format");
            if (newFormatObject == null) {
                return Result.error(ResultCode.WRONG_REQUEST_DATA);
            }
            newFormat = (String) newFormatObject;
        } catch (ClassCastException e) {
            return Result.error(ResultCode.WRONG_REQUEST_DATA);
        }
        if (newFormat.equals("default")) {
            newFormat = "[%groupNickname%] <%senderGroupNickname%> %message%";
        }
        System.out.println("newFormat = " + newFormat);

        List<Object> newRawGroups;
        try {
            Object newGroupsObject = requestSessionMap.get("groups");
            if (newGroupsObject == null) {
                return Result.error(ResultCode.WRONG_REQUEST_DATA);
            }
            newRawGroups = (List<Object>) newGroupsObject;
        } catch (ClassCastException e) {
            return Result.error(ResultCode.WRONG_REQUEST_DATA);
        }
        System.out.println("newRawGroups = " + newRawGroups);

        List<Object> newRawAdministrators;
        try {
            Object newAdministratorsObject = requestSessionMap.get("administrators");
            if (newAdministratorsObject == null) {
                return Result.error(ResultCode.WRONG_REQUEST_DATA);
            }
            newRawAdministrators = (List<Object>) newAdministratorsObject;
        } catch (ClassCastException e) {
            return Result.error(ResultCode.WRONG_REQUEST_DATA);
        }
        System.out.println("newRawAdministrators = " + newRawAdministrators);

        List<Long> newGroupIds = new ArrayList<>(newRawGroups.size());
        List<String> newGroupNames = new ArrayList<>(newRawGroups.size());
        for (Object newRawGroup : newRawGroups) {
            Map<String, Object> map;

//            检查是否为map类型
            try {
                map = (Map<String, Object>) newRawGroup;
            } catch (ClassCastException e) {
                return Result.error(ResultCode.WRONG_REQUEST_DATA);
            }

//            检查内容
            long groupId;
            try {
                Object groupIdObject = map.get("id");
                if (groupIdObject == null) {
                    return Result.error(ResultCode.WRONG_REQUEST_DATA);
                }
                groupId = groupIdObject instanceof Integer ? (Integer) groupIdObject : (Long) groupIdObject;
            } catch (ClassCastException e ) {
                return Result.error(ResultCode.WRONG_REQUEST_DATA);
            }
            newGroupIds.add(groupId);

            String groupName;
            try {
                Object groupNameObject = map.get("name");
                if (groupNameObject == null) {
                    return Result.error(ResultCode.WRONG_REQUEST_DATA);
                }
                groupName = (String) groupNameObject;
            } catch (ClassCastException e) {
                return Result.error(ResultCode.WRONG_REQUEST_DATA);
            }
            newGroupNames.add(groupName);
        }
        System.out.println("newGroupIds = " + newGroupIds);
        System.out.println("newGroupNames = " + newGroupNames);

        List<Long> newAdministrators = new ArrayList<>(newRawAdministrators.size());
        for (Object newRawAdministrator : newRawAdministrators) {
            long administrator;
            try {
                administrator = newRawAdministrator instanceof Integer ? (Integer) newRawAdministrator : (Long) newRawAdministrator;
            } catch (ClassCastException e) {
                return Result.error(ResultCode.WRONG_REQUEST_DATA);
            }
            newAdministrators.add(administrator);
        }
        System.out.println("newAdministrators = " + newAdministrators);

//        正式开始添加
        try {
            SessionDataOperation.addSessionData(newId, newName, newFormat);
        } catch (SessionDataExistException e) {
            return Result.error(ResultCode.SESSION_EXISTED);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(ResultCode.INNER_ERROR);
        }

        for (int i = 0; i < newGroupIds.size(); i++) {
            try {
                SessionDataOperation.addSessionDataGroup(newId, newGroupIds.get(i), newGroupNames.get(i));
            } catch (SessionDataNotExistException | SessionDataGroupExistException ignored) {

            } catch (Exception e) {
                e.printStackTrace();
                return Result.error(ResultCode.INNER_ERROR);
            }
        }

        for (Long newAdministrator : newAdministrators) {
            try {
                SessionDataOperation.addSessionDataAdministrator(newId, newAdministrator);
            } catch (SessionDataNotExistException | SessionDataAdministratorExistException ignored) {

            } catch (Exception e) {
                e.printStackTrace();
                return Result.error(ResultCode.INNER_ERROR);
            }
        }

        return Result.success("已成功添加会话" + newId + "(" + newName + ")");
    }
}
