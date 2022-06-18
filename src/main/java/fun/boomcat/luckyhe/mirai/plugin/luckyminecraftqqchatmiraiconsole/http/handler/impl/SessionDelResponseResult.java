package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.handler.impl;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.data.SessionDataOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.exception.SessionDataNotExistException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.handler.ResponseResult;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils.Result;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils.ResultCode;

import java.util.Map;

public class SessionDelResponseResult implements ResponseResult {
    @Override
    public Result<?> handle(Map<String, Object> jsonMap) {
        long id;
        try {
            Object idObject = jsonMap.get("id");
            id = idObject instanceof Integer ? (Integer) idObject : (Long) idObject;
        } catch (ClassCastException | NullPointerException e) {
            return Result.error(ResultCode.WRONG_REQUEST_DATA);
        }

        try {
            SessionDataOperation.removeSessionData(id);
        } catch (SessionDataNotExistException e) {
            return Result.error(ResultCode.SESSION_NOT_EXISTED);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(ResultCode.INNER_ERROR);
        }

        return Result.success("已成功删除会话" + id);
    }
}
