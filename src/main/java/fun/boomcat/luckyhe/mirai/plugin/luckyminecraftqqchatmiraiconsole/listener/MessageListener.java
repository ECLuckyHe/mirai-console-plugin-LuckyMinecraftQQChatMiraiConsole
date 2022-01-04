package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.listener;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.SessionUtil;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;

public class MessageListener extends SimpleListenerHost {
    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        super.handleException(context, exception);
    }

    @EventHandler
    public void onGroupMessageEvent(GroupMessageEvent e) {
        try {
            SessionUtil.sendMessageFromGroup(
                    e.getBot(),
                    e.getGroup().getId(),
                    e.getGroup().getName(),
                    e.getSender().getId(),
                    e.getSender().getNick(),
                    e.getSender().getNameCard(),
                    e.getMessage()
            );
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}
