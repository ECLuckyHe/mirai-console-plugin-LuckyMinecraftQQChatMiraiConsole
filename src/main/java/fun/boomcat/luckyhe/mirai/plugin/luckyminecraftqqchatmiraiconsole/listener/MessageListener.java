package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.listener;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.SessionUtil;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import org.jetbrains.annotations.NotNull;

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
                    e.getGroup(),
                    e.getSender(),
                    e.getMessage()
            );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
