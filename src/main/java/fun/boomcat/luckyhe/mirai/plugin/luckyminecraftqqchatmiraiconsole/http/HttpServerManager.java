package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http;

import com.sun.net.httpserver.HttpServer;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.config.ConfigOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.handler.ProxyHandler;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.handler.ResponseResult;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.handler.impl.SessionGetResponseResult;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils.HttpUtil;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils.Result;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils.ResultCode;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class HttpServerManager {
    private volatile static HttpServer httpServer;

    public static HttpServer getHttpServer() throws IOException {
        if (httpServer == null) {
            synchronized (HttpServerManager.class) {
                if (httpServer == null) {
                    httpServer = init();
                }
            }
        }
        return httpServer;
    }

    public static void startHttpServer() throws IOException {
        getHttpServer().start();
    }

    public static void stopHttpServer() throws IOException {
        getHttpServer().stop(0);
    }

    private static HttpServer init() throws IOException {
        HttpServer newHttpServer = HttpServer.create(new InetSocketAddress(ConfigOperation.getHttpManagePort()), 0);

        newHttpServer.createContext("/session/get", httpExchange -> {
            try {
                httpExchange.getResponseBody().write(Result.toJsonString(((ResponseResult) Proxy.newProxyInstance(
                        newHttpServer.getClass().getClassLoader(),
                        new Class[]{ResponseResult.class},
                        new ProxyHandler(new SessionGetResponseResult())
                )).handle(HttpUtil.getRequestMap(httpExchange))).getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                httpExchange.getResponseBody().write(Result.toJsonString(Result.error(ResultCode.WRONG_REQUEST_DATA))
                        .getBytes(StandardCharsets.UTF_8));
            } finally {
                httpExchange.getRequestBody().close();
                httpExchange.getResponseBody().close();
            }
        });
        newHttpServer.createContext("/session/list", httpExchange -> {

        });
        newHttpServer.createContext("/session/add", httpExchange -> {

        });
        newHttpServer.createContext("/session/del", httpExchange -> {

        });

        return newHttpServer;
    }
}
