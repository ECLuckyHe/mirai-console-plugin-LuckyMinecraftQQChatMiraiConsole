package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.command;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.opmcchatcommand.OpMcChatCommandStep;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.opmcchatcommand.OpMcChatCommandStepUtil;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import net.mamoe.mirai.console.command.CommandOwner;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.RawCommand;
import net.mamoe.mirai.console.permission.Permission;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChain;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpMcChatCommand extends RawCommand {
    public OpMcChatCommand(@NotNull CommandOwner owner, @NotNull String primaryName, @NotNull String[] secondaryNames, @NotNull String usage, @NotNull String description, @NotNull Permission parentPermission, boolean prefixOptional) {
        super(owner, primaryName, secondaryNames, usage, description, parentPermission, prefixOptional);
    }

    @Nullable
    @Override
    public Object onCommand(@NotNull CommandSender commandSender, @NotNull MessageChain messageChain, @NotNull Continuation<? super Unit> continuation) {
        User user = commandSender.getUser();
        Contact subject = commandSender.getSubject();
        if (user == null || subject == null) {
            commandSender.sendMessage("请在QQ中发送该指令");
            return null;
        }

        if (subject.getId() != user.getId()) {
            commandSender.sendMessage("请私聊该指令");
            return null;
        }

        OpMcChatCommandStep step = OpMcChatCommandStepUtil.getStep(user.getId());
        if (step != null) {
            commandSender.sendMessage("现在正在执行该指令，请按照提示完成");
            return null;
        }

        OpMcChatCommandStepUtil.setStep(user.getId(), OpMcChatCommandStep.MAIN, commandSender.getSubject());
        return null;
    }
}
