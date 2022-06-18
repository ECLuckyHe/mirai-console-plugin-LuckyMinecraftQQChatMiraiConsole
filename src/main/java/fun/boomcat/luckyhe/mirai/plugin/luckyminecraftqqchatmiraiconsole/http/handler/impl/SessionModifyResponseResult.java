package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.handler.impl;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.data.SessionDataOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.*;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.handler.ResponseResult;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils.Result;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils.ResultCode;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.Session;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.SessionGroup;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.SessionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SessionModifyResponseResult implements ResponseResult {
    @Override
    public Result<?> handle(Map<String, Object> jsonMap) {
        Map<String, Object> requestSessionMap;
        try {
            requestSessionMap = (Map<String, Object>) jsonMap.get("session");
        } catch (ClassCastException | NullPointerException e) {
            return Result.error(ResultCode.WRONG_REQUEST_DATA);
        }

        long id;
        try {
            Object idObject = requestSessionMap.get("id");
            id = idObject instanceof Integer ? (Integer) idObject : (Long) idObject;
        } catch (ClassCastException | NullPointerException e) {
            return Result.error(ResultCode.WRONG_REQUEST_DATA);
        }

        Session session;
        try {
            session = SessionUtil.getSession(id);
        } catch (SessionDataNotExistException e) {
            return Result.error(ResultCode.SESSION_NOT_EXISTED);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(ResultCode.INNER_ERROR);
        }

        String newName;
        try {
            newName = (String) requestSessionMap.get("name");
        } catch (ClassCastException e) {
            return Result.error(ResultCode.WRONG_REQUEST_DATA);
        } catch (NullPointerException e) {
//            如果为null，则不处理
            newName = null;
        }

        String newFormat;
        try {
            newFormat = (String) requestSessionMap.get("format");
        } catch (ClassCastException e) {
            return Result.error(ResultCode.WRONG_REQUEST_DATA);
        } catch (NullPointerException ignored) {
            newFormat = null;
        }
        if (newFormat != null && newFormat.equals("default")) {
            newFormat = "[%groupNickname%] <%senderGroupNickname%> %message%";
        }

        List<Object> newRawGroups;
        try {
            newRawGroups = (List<Object>) requestSessionMap.get("groups");
        } catch (ClassCastException e) {
            return Result.error(ResultCode.WRONG_REQUEST_DATA);
        } catch (NullPointerException e) {
            newRawGroups = null;
        }

        List<Object> newRawAdministrators;
        try {
            newRawAdministrators = (List<Object>) requestSessionMap.get("administrators");
        } catch (ClassCastException e) {
            return Result.error(ResultCode.WRONG_REQUEST_DATA);
        } catch (NullPointerException e) {
            newRawAdministrators = null;
        }


        List<SessionGroup> newGroups = null;
        if (newRawGroups != null) {
            newGroups = new ArrayList<>(newRawGroups.size());
            for (Object newRawGroup : newRawGroups) {
                Map<String, Object> map;

                try {
                    //                检查类型
                    map = (Map<String, Object>) newRawGroup;
                } catch (ClassCastException e) {
                    return Result.error(ResultCode.WRONG_REQUEST_DATA);
                }

                //            检查内容
                long groupId;
                try {
                    Object groupIdObject = map.get("id");
                    groupId = groupIdObject instanceof Integer ? (Integer) groupIdObject : (Long) groupIdObject;
                } catch (ClassCastException | NullPointerException e) {
                    return Result.error(ResultCode.WRONG_REQUEST_DATA);
                }

                String groupName;
                try {
                    groupName = (String) map.get("name");
                } catch (ClassCastException e) {
                    return Result.error(ResultCode.WRONG_REQUEST_DATA);
                }

                if (groupName == null) {
//                    没有提供群昵称
                    return Result.error(ResultCode.WRONG_REQUEST_DATA);
                }

                newGroups.add(new SessionGroup(groupId, groupName));
            }
        }

        List<Long> newAdministrators = null;
        if (newRawAdministrators != null) {
            newAdministrators = new ArrayList<>(newRawAdministrators.size());
            for (Object newRawAdministrator : newRawAdministrators) {
                long administrator;

                try {
                    administrator = newRawAdministrator instanceof Integer ? (Integer) newRawAdministrator : (Long) newRawAdministrator;
                } catch (ClassCastException e) {
                    return Result.error(ResultCode.WRONG_REQUEST_DATA);
                }

                newAdministrators.add(administrator);
            }
        }

//        开始修改会话，后续判断相关变量是否为null即可

//        对会话名进行比较与修改，下面相同
        if (newName != null && !newName.equals(session.getName())) {
            try {
                SessionDataOperation.modifySessionDataName(id, newName);
            } catch (SessionDataNotExistException e) {
                return Result.error(ResultCode.SESSION_NOT_EXISTED);
            } catch (Exception e) {
                return Result.error(ResultCode.INNER_ERROR);
            }
        }

        if (newFormat != null && !newFormat.equals(session.getFormatString())) {
            try {
                SessionDataOperation.modifySessionDataFormat(id, newFormat);
            } catch (SessionDataNotExistException e) {
                return Result.error(ResultCode.SESSION_NOT_EXISTED);
            } catch (Exception e) {
                return Result.error(ResultCode.INNER_ERROR);
            }
        }

        if (newGroups != null) {
//            例：当前[1, 2, 3, 4]，传入群为[1, 3, 5, 6, 7]

//            先对传入的群中的新群进行添加
            List<SessionGroup> currentGroups = new ArrayList<>(session.getGroups());
            for (SessionGroup newGroup : newGroups) {
                if (currentGroups.stream().noneMatch(ele -> ele.getId() == newGroup.getId())) {
//                    如果不存在，则添加
                    try {
                        SessionDataOperation.addSessionDataGroup(id, newGroup.getId(), newGroup.getName());
                    } catch (SessionDataNotExistException | SessionDataGroupExistException ignored) {
//                        不处理，因为此处不会出现这两种异常
                    } catch (Exception e) {
//                        不处理，仅仅输出
                        e.printStackTrace();
                    }

//                    同时给 currentGroups 添加（该对象在该方法执行后已经无用）
                    currentGroups.add(newGroup);
                }
            }
//            添加完成后当前[1, 2, 3, 4, 5, 6, 7]，传入群[1, 3, 5, 6, 7]

//            再对传入的群中不存在的群进行删除
            for (SessionGroup currentGroup : currentGroups) {
                if (newGroups.stream().noneMatch(ele -> ele.getId() == currentGroup.getId())) {
//                    若不存在，则为需要删除的
                    try {
                        SessionDataOperation.removeSessionDataGroup(id, currentGroup.getId());
                    } catch (SessionDataNotExistException | SessionDataGroupNotExistException e) {
                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

//            删除完成后当前[1, 3, 5, 6, 7]
        }

        if (newAdministrators != null) {
            List<Long> currentAdministrators = new ArrayList<>(session.getAdministrators());
            for (Long newAdministrator : newAdministrators) {
                if (currentAdministrators.stream().noneMatch(e -> e.equals(newAdministrator))) {
                    try {
                        SessionDataOperation.addSessionDataAdministrator(id, newAdministrator);
                    } catch (SessionDataAdministratorExistException | SessionDataNotExistException ignored) {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            for (Long currentAdministrator : currentAdministrators) {
                if (newAdministrators.stream().noneMatch(e -> e.equals(currentAdministrator))) {
                    try {
                        SessionDataOperation.removeSessionDataAdministrator(id, currentAdministrator);
                    } catch (SessionDataNotExistException | SessionDataAdministratorNotExistException ignored) {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return Result.success("已成功修改会话" + id + "(" + session.getName() + ")");
    }
}
