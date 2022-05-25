package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http;

import com.sun.net.httpserver.HttpServer;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.config.ConfigOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.handler.impl.SessionAddResponseResult;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.handler.impl.SessionGetResponseResult;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.handler.impl.SessionListResponseResult;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils.HttpUtil;

import java.io.IOException;
import java.net.InetSocketAddress;

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

        newHttpServer.createContext("/session/get", httpExchange -> HttpUtil.handleRequest(httpExchange, new SessionGetResponseResult()));
        newHttpServer.createContext("/session/list", httpExchange -> HttpUtil.handleRequest(httpExchange, new SessionListResponseResult()));
        newHttpServer.createContext("/session/add", httpExchange -> HttpUtil.handleRequest(httpExchange, new SessionAddResponseResult()));
        newHttpServer.createContext("/session/del", httpExchange -> {

        });

        return newHttpServer;
    }
}
