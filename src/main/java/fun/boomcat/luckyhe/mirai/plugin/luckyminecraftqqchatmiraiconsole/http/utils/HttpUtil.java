package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils;

import com.alibaba.fastjson.JSONObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.config.ConfigOperation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

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

    public static Map<String, Object> getRequestMap(HttpExchange httpExchange) throws IOException {
        InputStream inputStream = httpExchange.getRequestBody();
        setResponseHeaders(httpExchange.getResponseHeaders());

        int len;
        byte[] buf = new byte[1024];
        StringBuilder sb = new StringBuilder();
        while ((len = inputStream.read(buf)) > 0) {
            sb.append(new String(buf, 0, len));
        }

        return (Map<String, Object>) JSONObject.parse(sb.toString());
    }
}
