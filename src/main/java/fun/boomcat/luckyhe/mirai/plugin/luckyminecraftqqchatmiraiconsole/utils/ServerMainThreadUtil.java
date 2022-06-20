package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.thread.ServerMainThread;
import net.mamoe.mirai.utils.MiraiLogger;

public class ServerMainThreadUtil {
    private volatile static ServerMainThread serverMainThread;

    public static void startNewThread() {
        if (serverMainThread == null) {
            synchronized (ServerMainThreadUtil.class) {
                if (serverMainThread == null) {
                    serverMainThread = new ServerMainThread(MiraiLoggerUtil.getLogger());

                    serverMainThread.start();
                    MiraiLoggerUtil.getLogger().info("已开启监听线程");
                }
            }
        }
    }

    public static void deleteCurrentThread(String exitMessage) {
        if (serverMainThread != null) {
            synchronized (ServerMainThreadUtil.class) {
                if (serverMainThread != null) {
                    MiraiLogger logger = MiraiLoggerUtil.getLogger();

                    logger.info("=================================================================");

                    while (serverMainThread.isAlive()) {
                        serverMainThread.close();
                    }
                    logger.info("监听线程关闭完成");

                    try {
                        SessionUtil.closeAllConnections(exitMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    logger.info("已关闭所有线程");
                    logger.info("=================================================================");

                    serverMainThread = null;
                }
            }
        }
    }
}
