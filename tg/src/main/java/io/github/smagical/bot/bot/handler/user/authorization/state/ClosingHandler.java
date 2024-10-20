package io.github.smagical.bot.bot.handler.user.authorization.state;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.bot.handler.base.BaseHandlerWrapper;
import io.github.smagical.bot.event.BaseEvent;
import org.drinkless.tdlib.TdApi;

public class ClosingHandler extends BaseHandlerWrapper {
    public ClosingHandler(Bot bot) {
        super(bot);
    }

    public  class ClosingEvent extends BaseEvent implements AuthorizationStateDispatchHandler.AuthorizationStateEvent{
        private ClosingEvent() {
            super(null);
        }
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.AuthorizationStateClosed state = (TdApi.AuthorizationStateClosed) object;
        getBot().send(new ClosingEvent());
    }

    @Override
    public int[] support() {
        return new int[]{TdApi.AuthorizationStateClosing.CONSTRUCTOR};
    }
}