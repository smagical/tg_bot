package io.github.smagical.bot.bot.handler.user.authorization.state;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.bot.handler.base.BaseHandlerWrapper;
import io.github.smagical.bot.event.BaseEvent;
import org.drinkless.tdlib.TdApi;

public class ClosedHandler extends BaseHandlerWrapper {
    public ClosedHandler(Bot bot) {
        super(bot);
    }

    public  class Closed extends BaseEvent{
        private Closed() {
            super(null);
        }
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.AuthorizationStateClosed state = (TdApi.AuthorizationStateClosed) object;
        getBot().send(new Closed());
    }

    @Override
    public int[] support() {
        return new int[]{TdApi.AuthorizationStateClosed.CONSTRUCTOR};
    }
}
