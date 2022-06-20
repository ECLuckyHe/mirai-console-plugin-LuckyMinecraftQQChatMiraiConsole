package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.config.ConfigOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.handler.ResponseResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpUtil {
    public static boolean checkVerifyPassword(String password) {
        try {
            return password.equals(ConfigOperation.getHttpManagePassword());
        } catch (Exception e) {
            return false;
        }
    }

    public static void handleRequest(HttpExchange httpExchange, ResponseResult responseResult) {
        InputStream inputStream = httpExchange.getRequestBody();
        OutputStream outputStream = httpExchange.getResponseBody();

        Headers responseHeaders = httpExchange.getResponseHeaders();
        responseHeaders.set("Content-Type", "application/json; charset=utf-8");
        responseHeaders.set("Access-Control-Allow-Origin", "");
        responseHeaders.set("Access-Control-Allow-Methods", "POST");
        responseHeaders.set("Connection", "close");

        try {
//        获取json请求内容
            int len;
            byte[] buf = new byte[1024];
            StringBuilder sb = new StringBuilder();
            while ((len = inputStream.read(buf)) > 0) {
                sb.append(new String(buf, 0, len, StandardCharsets.UTF_8));
            }
            Map<String, Object> requestJson = (Map<String, Object>) JSONObject.parse(sb.toString());

//            获取验证密码
            String verifyPassword;
            try {
//                此处有可能会抛出 NullPointerException 异常
                verifyPassword = (String) requestJson.get("verifyPassword");
            } catch (ClassCastException | NullPointerException e) {
                byte[] bytes = Result.error(ResultCode.WRONG_REQUEST_DATA).toJsonBytes();
                httpExchange.sendResponseHeaders(200, bytes.length);
                outputStream.write(bytes);
                return;
            }

//            未提供验证密码
            if (verifyPassword == null) {
                byte[] bytes = Result.error(ResultCode.WRONG_REQUEST_DATA).toJsonBytes();
                httpExchange.sendResponseHeaders(200, bytes.length);
                outputStream.write(bytes);
                return;
            }

//            判断验证密码
            if (!checkVerifyPassword(verifyPassword)) {
                byte[] bytes = Result.error(ResultCode.VERIFY_NOT_PASSED).toJsonBytes();
                httpExchange.sendResponseHeaders(200, bytes.length);
                outputStream.write(bytes);
                return;
            }

//            操作结果
            byte[] bytes = responseResult.handle(requestJson).toJsonBytes();
            httpExchange.sendResponseHeaders(200, bytes.length);
            outputStream.write(bytes);
        } catch (JSONException e) {
//            JSON转换出错
            byte[] bytes = Result.error(ResultCode.WRONG_REQUEST_FORMAT).toJsonBytes();
            try {
                httpExchange.sendResponseHeaders(200, bytes.length);
                outputStream.write(bytes);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
//            其它异常
            e.printStackTrace();

            byte[] bytes = Result.error(ResultCode.INNER_ERROR).toJsonBytes();
            try {
                httpExchange.sendResponseHeaders(200, bytes.length);
                outputStream.write(bytes);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
