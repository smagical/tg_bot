package io.github.smagical.bot.bot.handler.chat.update;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.bot.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatPositionHandler extends BaseHandlerWrapper {
    public ChatPositionHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatPosition chatPosition = (TdApi.UpdateChatPosition) object;
        log.debug("\n{}",chatPosition.toString());

        //todo 麻烦

        TdApi.Chat chat = getBot().getChat(chatPosition.chatId);
        if (chat != null) {
            synchronized (chat){

            }
        }
    }

    protected void solveChat(TdApi.Chat chat,TdApi.UpdateChatPosition chatPosition) {

    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatPosition.CONSTRUCTOR
        };
    }
}