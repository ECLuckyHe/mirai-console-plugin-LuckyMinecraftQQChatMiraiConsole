package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.command.McChatCommand;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.command.OpMcChatCommand;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.config.ConfigOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.data.SessionDataOperation;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.listener.McCommandStepListener;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.listener.MessageListener;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.listener.OpMcCommandStepListener;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LuckyMinecraftQQChatMiraiConsole extends JavaPlugin {
    public static final LuckyMinecraftQQChatMiraiConsole INSTANCE = new LuckyMinecraftQQChatMiraiConsole();

    private Permission opMcChatPerm;
    private Permission mcChatPerm;

    private final String opMcChatCommandPrimaryName = "opmcchat";
    private final String[] opMcChatCommandSecondaryNames = {"mc????????????", "????????????"};
    private final String mcChatCommandPrimaryName = "mcchat";
    private final String[] mcChatCommandSecondaryNames = {"mc??????", "??????"};

    public String getOpMcChatCommandPrimaryName() {
        return opMcChatCommandPrimaryName;
    }

    public String[] getOpMcChatCommandSecondaryNames() {
        return opMcChatCommandSecondaryNames;
    }

    public String getMcChatCommandPrimaryName() {
        return mcChatCommandPrimaryName;
    }

    public String[] getMcChatCommandSecondaryNames() {
        return mcChatCommandSecondaryNames;
    }

    //    ???????????????
    private ServerMainThread serverMainThread;

    private LuckyMinecraftQQChatMiraiConsole() {
        super(new JvmPluginDescriptionBuilder(
                "luckyhe.luckyminecraftqqchatmiraiconsole",
                "1.2"
        ).build());
    }

    public ServerMainThread getServerMainThread() {
        return serverMainThread;
    }

    public void newServerMainThread() {
        serverMainThread = new ServerMainThread(getLogger());
    }

    public void stopServerMainThread(String exitMessage) {
        getLogger().info("=================================================================");

        while (serverMainThread.isAlive()) {
            serverMainThread.close();
        }
        getLogger().info("????????????????????????");

        try {
            SessionUtil.closeAllConnections(exitMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        getLogger().info("?????????????????????");
        getLogger().info("=================================================================");
    }

    @Override
    public void onDisable() {
        List<Session> sessions = null;
        try {
            sessions = SessionUtil.getSessions();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        ??????????????????????????????
        stopServerMainThread("bot????????????");
    }

    @Override
    public void onEnable() {
        INSTANCE.getLogger().info("??????????????????");
        loadPermissions();
        INSTANCE.getLogger().info("??????????????????");
        loadCommands();
        INSTANCE.getLogger().info("??????????????????");
        loadListeners();
        INSTANCE.getLogger().info("?????????????????????");
        INSTANCE.getLogger().info("========================");
        INSTANCE.getLogger().info("??????????????????????????????bot??????");
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
            SessionDataOperation.initSessionDataPath(getDataFolder(), getResource("sessionData.yml", StandardCharsets.UTF_8), INSTANCE);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        ?????????????????????
        newServerMainThread();
        serverMainThread.start();
    }

    private void loadPermissions() {
        PermissionId opMcChatPermId = new PermissionId("luckyminecraftqqchatmiraiconsole", "command.opmcchat");
        PermissionId mcChatPermId = new PermissionId("luckyminecraftqqchatmiraiconsole", "command.mcchat");

        try {
            opMcChatPerm = PermissionService.getInstance().register(
                    opMcChatPermId,
                    "mc????????????????????????",
                    Permission.getRootPermission()
            );
        } catch (PermissionRegistryConflictException e) {
            e.printStackTrace();
        }

        try {
            mcChatPerm = PermissionService.getInstance().register(
                    mcChatPermId,
                    "mc??????????????????",
                    Permission.getRootPermission()
            );
        } catch (PermissionRegistryConflictException e) {
            e.printStackTrace();
        }
    }

    private void loadCommands() {
        String commandPrefix = CommandManager.INSTANCE.getCommandPrefix();

        CommandManager.INSTANCE.registerCommand(new OpMcChatCommand(
                this,
                opMcChatCommandPrimaryName,
                opMcChatCommandSecondaryNames,
                commandPrefix + opMcChatCommandPrimaryName + " <??????>",
                "mc????????????????????????",
                opMcChatPerm,
                false
        ), false);

        CommandManager.INSTANCE.registerCommand(new McChatCommand(
                this,
                mcChatCommandPrimaryName,
                mcChatCommandSecondaryNames,
                commandPrefix + mcChatCommandPrimaryName + " <??????>",
                "mc??????????????????",
                mcChatPerm,
                false
        ), false);
    }

    private void loadListeners() {
        GlobalEventChannel.INSTANCE.registerListenerHost(new MessageListener());
        GlobalEventChannel.INSTANCE.registerListenerHost(new OpMcCommandStepListener(this));
        GlobalEventChannel.INSTANCE.registerListenerHost(new McCommandStepListener(this));
    }
}
