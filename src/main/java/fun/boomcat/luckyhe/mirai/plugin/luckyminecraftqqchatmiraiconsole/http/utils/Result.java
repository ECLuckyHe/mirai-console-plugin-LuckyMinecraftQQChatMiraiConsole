package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Result<T> {
    private final int code;
    private final T data;
    private final String msg;

    private Result(ResultCode resultCode, T data) {
        if (resultCode == null) {
            this.code = -1;
            this.msg = null;
        } else {
            this.code = resultCode.getCode();
            this.msg = resultCode.getMsg();
        }
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public T getData() {
        return data;
    }

    public String getMsg() {
        return msg;
    }

    public static <T> String success(T data) {
        Result<T> result = new Result<>(ResultCode.SUCCESS, data);
        return toJsonString(result);
    }

    public static <T> String error(ResultCode resultCode) {
        Result<T> result = new Result<>(resultCode, null);
        return toJsonString(result);
    }

    private static String toJsonString(Result<?> result) {
        Map<String, Object> jsonMap = new HashMap<>();

        int code = result.getCode();
        String msg = result.getMsg();
        Object data = result.getData();

        if (code != -1) {
            jsonMap.put("code", code);
        }
        if (msg != null) {
            jsonMap.put("msg", msg);
        }
        if (data != null) {
            jsonMap.put("data", data);
        }

        return JSONObject.toJSONString(jsonMap);
    }
}
