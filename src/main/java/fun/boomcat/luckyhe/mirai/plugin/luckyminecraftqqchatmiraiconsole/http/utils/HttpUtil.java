package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils;

import com.sun.net.httpserver.Headers;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.config.ConfigOperation;

import java.io.FileNotFoundException;

public class HttpUtil {
    public static void setResponseHeaders(Headers responseHeaders) {
        responseHeaders.set("Content-Type", "application/json; charset=utf-8");
        responseHeaders.set("Access-Control-Allow-Origin", "");
        responseHeaders.set("Access-Control-Allow-Methods", "POST");
    }

    public static boolean checkVerifyPassword(String password) {
        try {
            return password.equals(ConfigOperation.getHttpManagePassword());
        } catch (FileNotFoundException e) {
            return false;
        }
    }
}
