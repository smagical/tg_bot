package io.github.smagical.bot.bot.handler.chat.update.message;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.bot.handler.base.BaseHandlerWrapper;
import io.github.smagical.bot.bot.handler.chat.update.ui.ChatPositionHandler;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatLastMessageHandler extends BaseHandlerWrapper {
    public ChatLastMessageHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatLastMessage lastMessage = (TdApi.UpdateChatLastMessage) object;
        log.debug("lastMessage:\n {}", lastMessage);
        TdApi.Chat chat = getBot().getChat(lastMessage.chatId);
        synchronized (chat) {
            chat.lastMessage = lastMessage.lastMessage;
            getBot().send(
                    new ChatPositionHandler.ChatPositionUpdateEvent(
                            new ChatPositionHandler.ChatPositionUpdateEvent.Data(
                                    lastMessage.chatId,
                                    lastMessage.positions
                            )
                    )
            );
            getBot().send(
                    new LastMessageEvent(lastMessage)
            );
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatLastMessage.CONSTRUCTOR
        };
    }

    public class LastMessageEvent extends io.github.smagical.bot.event.message.MessageEvent<TdApi.UpdateChatLastMessage> {
        private LastMessageEvent(TdApi.UpdateChatLastMessage code) {
            super(code);
        }
    }
}