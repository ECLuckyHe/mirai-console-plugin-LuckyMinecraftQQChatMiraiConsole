package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.command.McChatCommand;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.config.ConfigOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.data.SessionDataOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.listener.MessageListener;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.thread.ServerThread;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.MiraiLoggerUtil;
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

public class LuckyMinecraftQQChatMiraiConsole extends JavaPlugin {
    public static final LuckyMinecraftQQChatMiraiConsole INSTANCE = new LuckyMinecraftQQChatMiraiConsole();

    private Permission mcChatPerm;

    private LuckyMinecraftQQChatMiraiConsole() {
        super (new JvmPluginDescriptionBuilder(
                "luckyhe.luckyminecraftqqchatmiraiconsole",
                "1.0"
        ).build());
    }

    @Override
    public void onDisable() {
        super.onDisable();
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

        new ServerThread(getLogger()).start();

    }

    private void loadPermissions() {
        PermissionId mcChatPermId = new PermissionId("luckyminecraftqqchatmiraiconsole", "command.mcchat");

        try {
            mcChatPerm = PermissionService.getInstance().register(
                    mcChatPermId,
                    "mc互通相关指令",
                    Permission.getRootPermission()
            );
        } catch (PermissionRegistryConflictException e) {
            e.printStackTrace();
        }
    }

    private void loadCommands() {
        String commandPrefix = CommandManager.INSTANCE.getCommandPrefix();

        String[] mcChatCommandSecondaryNames = {"mc互通", "互通"};
        CommandManager.INSTANCE.registerCommand(new McChatCommand(
                this,
                "mcchat",
                mcChatCommandSecondaryNames,
                commandPrefix + "mcchat <操作>",
                "mc互通相关指令",
                mcChatPerm,
                false
        ), false);
    }

    private void loadListeners() {
        GlobalEventChannel.INSTANCE.registerListenerHost(new MessageListener());
    }
}
