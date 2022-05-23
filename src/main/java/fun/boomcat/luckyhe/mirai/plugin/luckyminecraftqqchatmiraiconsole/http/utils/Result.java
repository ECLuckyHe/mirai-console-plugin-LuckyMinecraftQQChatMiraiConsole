package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils;

import com.alibaba.fastjson.JSONObject;

import java.nio.charset.StandardCharsets;
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

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS, data);
    }

    public static Result<?> error(ResultCode resultCode) {
        return new Result<>(resultCode, null);
    }

    public String toJsonString() {
        Map<String, Object> jsonMap = new HashMap<>();

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

    public byte[] toJsonBytes() {
        return toJsonString().getBytes(StandardCharsets.UTF_8);
    }
}
