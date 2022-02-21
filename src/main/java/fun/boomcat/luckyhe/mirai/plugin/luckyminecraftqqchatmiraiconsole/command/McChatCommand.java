package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.command;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.mcchatcommand.McChatCommandStep;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.mcchatcommand.McChatCommandStepUtil;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import net.mamoe.mirai.console.command.CommandOwner;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.RawCommand;
import net.mamoe.mirai.console.permission.Permission;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChain;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class McChatCommand extends RawCommand {
    public McChatCommand(@NotNull CommandOwner owner, @NotNull String primaryName, @NotNull String[] secondaryNames, @NotNull String usage, @NotNull String description, @NotNull Permission parentPermission, boolean prefixOptional) {
        super(owner, primaryName, secondaryNames, usage, description, parentPermission, prefixOptional);
    }

    @Nullable
    @Override
    public Object onCommand(@NotNull CommandSender commandSender, @NotNull MessageChain messageChain, @NotNull Continuation<? super Unit> continuation) {
        User user = commandSender.getUser();
        if (user == null) {
            commandSender.sendMessage("请在QQ中发送该指令");
            return null;
        }

        McChatCommandStep step = McChatCommandStepUtil.getStep(user.getId());
        if (step != null) {
            commandSender.sendMessage("现在正在执行该指令，请按照提示完成");
            return null;
        }

        McChatCommandStepUtil.setStep(user.getId(), McChatCommandStep.MAIN, commandSender.getSubject());
        return null;
    }
}
