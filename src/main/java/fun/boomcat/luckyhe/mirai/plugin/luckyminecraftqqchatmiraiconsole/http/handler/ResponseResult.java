package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.handler;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils.Result;

import java.util.Map;

public interface ResponseResult {
    Result<?> handle(Map<String, Object> jsonMap);
}
