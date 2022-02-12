package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.command.OpMcChatCommand;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.config.ConfigOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.data.SessionDataOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.listener.MessageListener;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo.Session;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.thread.ServerMainThread;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.MiraiLoggerUtil;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.SessionUtil;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.extension.PluginComponentStorage;
import net.mamoe.mirai.console.permission.Permission;
import net.mamoe.mirai.console.permission.PermissionId;
import net.mamoe.mirai.console.permission.PermissionRegistryConflictException;
import net.mamoe.mirai.console.permission.PermissionService;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LuckyMinecraftQQChatMiraiConsole extends JavaPlugin {
    public static final LuckyMinecraftQQChatMiraiConsole INSTANCE = new LuckyMinecraftQQChatMiraiConsole();

    private Permission opMcChatPerm;

//    监听主线程
    private ServerMainThread serverMainThread = new ServerMainThread(getLogger());

    private LuckyMinecraftQQChatMiraiConsole() {
        super(new JvmPluginDescriptionBuilder(
                "luckyhe.luckyminecraftqqchatmiraiconsole",
                "1.1.1"
        ).build());
    }

    @Override
    public void onDisable() {
        List<Session> sessions = null;
        try {
            sessions = SessionUtil.getSessions();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        关闭所有游戏连接线程
        getLogger().info("=================================================================");

        while (serverMainThread.isAlive()) {
            serverMainThread.close();
        }
        getLogger().info("监听线程关闭完成");

        try {
            SessionUtil.closeAllConnections("bot执行退出bot进程指令");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        getLogger().info("已关闭所有线程");
        getLogger().info("=================================================================");
    }

    @Override
    public void onEnable() {
        INSTANCE.getLogger().info("开始加载插件");
        loadPermissions();
        INSTANCE.getLogger().info("注册权限完成");
        loadCommands();
        INSTANCE.getLogger().info("注册指令完成");
        loadListeners();
        INSTANCE.getLogger().info("注册监听器完成");
        INSTANCE.getLogger().info("========================");
        INSTANCE.getLogger().info("注意：该插件只支持单bot运行");
        INSTANCE.getLogger().info("========================");
    }

    @Override
    public void onLoad(@NotNull PluginComponentStorage $this$onLoad) {
        MiraiLoggerUtil.init(INSTANCE.getLogger());

        try {
            ConfigOperation.initConfigPath(getConfigFolder(), getResource("config.yml", StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            SessionDataOperation.initSessionDataPath(getDataFolder(), getResource("sessionData.yml", StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

        serverMainThread.start();

    }

    private void loadPermissions() {
        PermissionId opMcChatPermId = new PermissionId("luckyminecraftqqchatmiraiconsole", "command.opmcchat");

        try {
            opMcChatPerm = PermissionService.getInstance().register(
                    opMcChatPermId,
                    "mc互通相关管理指令",
                    Permission.getRootPermission()
            );
        } catch (PermissionRegistryConflictException e) {
            e.printStackTrace();
        }
    }

    private void loadCommands() {
        String commandPrefix = CommandManager.INSTANCE.getCommandPrefix();

        String[] mcChatCommandSecondaryNames = {"mc互通管理", "互通管理"};
        CommandManager.INSTANCE.registerCommand(new OpMcChatCommand(
                this,
                "opmcchat",
                mcChatCommandSecondaryNames,
                commandPrefix + "opmcchat <操作>",
                "mc互通相关指令",
                opMcChatPerm,
                false
        ), false);
    }

    private void loadListeners() {
        GlobalEventChannel.INSTANCE.registerListenerHost(new MessageListener());
    }
}
