package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.command;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.McChatCommandUtil;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.SessionCommandHandler;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.command.CommandOwner;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.RawCommand;
import net.mamoe.mirai.console.permission.Permission;
import net.mamoe.mirai.message.data.MessageChain;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class McChatCommand extends RawCommand {
    public McChatCommand(@NotNull CommandOwner owner, @NotNull String primaryName, @NotNull String[] secondaryNames, @NotNull String usage, @NotNull String description, @NotNull Permission parentPermission, boolean prefixOptional) {
        super(owner, primaryName, secondaryNames, usage, description, parentPermission, prefixOptional);
    }

    @Nullable
    @Override
    public Object onCommand(@NotNull CommandSender commandSender, @NotNull MessageChain messageChain, @NotNull Continuation<? super Unit> continuation) {
        String primaryName = getPrimaryName();
        String[] secondaryNames = getSecondaryNames();
        String commandPrefix = CommandManager.INSTANCE.getCommandPrefix();

        int mcLen = messageChain.size();
        if (mcLen == 0) {
            commandSender.sendMessage(McChatCommandUtil.mainHelp(primaryName, secondaryNames));
            return null;
        }

        String operation = messageChain.get(0).contentToString();
        switch (operation) {
            case "session":
                SessionCommandHandler.selectSession(
                        Arrays.copyOfRange(messageChain.toArray(), 1, mcLen),
                        commandSender,
                        primaryName,
                        secondaryNames
                );
                break;
            default:
                commandSender.sendMessage(McChatCommandUtil.mainHelp(primaryName, secondaryNames));
                return null;
        }

        return null;
    }
}
