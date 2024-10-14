package io.github.smagical.bot.bot.handler;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.bot.HandlerWrapper;
import org.drinkless.tdlib.TdApi;

public class UpdateOptionHandler implements HandlerWrapper {
    @Override
    public int[] support() {
        return new int[]{TdApi.UpdateOption.CONSTRUCTOR};
    }


    @Override
    public Bot getBot() {
        return null;
    }

    @Override
    public void onResult(TdApi.Object object) {

    }
}
