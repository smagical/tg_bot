package io.github.smagical.bot.bot.handler.chat.update.info;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.bot.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class BasicGroupHandler extends BaseHandlerWrapper {
    public BasicGroupHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateBasicGroup group = (TdApi.UpdateBasicGroup) object;
        log.debug("Update group: \n{}", group);

    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateBasicGroup.CONSTRUCTOR
        };
    }
}
