package io.github.smagical.bot.bot.handler.chat.update;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.bot.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatDraftMessageHandler extends BaseHandlerWrapper {
    public ChatDraftMessageHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatDraftMessage chatDraftMessage = (TdApi.UpdateChatDraftMessage) object;
        log.debug("chatDraftMessage: \n{}", chatDraftMessage);
        TdApi.Chat chat = getBot().getChat(chatDraftMessage.chatId);
        synchronized (chat) {
            chat.draftMessage = chatDraftMessage.draftMessage;
            getBot().send(new ChatPositionHandler.ChatPositionUpdateEvent(
                    new ChatPositionHandler.ChatPositionUpdateEvent.Data(chatDraftMessage.chatId,chatDraftMessage.positions)
            ));
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatDraftMessage.CONSTRUCTOR
        };
    }
}