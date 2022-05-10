package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http;

import com.sun.net.httpserver.HttpServer;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.config.ConfigOperation;

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

        return newHttpServer;
    }
}
