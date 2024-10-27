package io.github.smagical.bot.plugin;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.bus.MessageDispatch;
import io.github.smagical.bot.event.Event;
import io.github.smagical.bot.listener.Listener;

public class PluginDispatch extends MessageDispatch implements Listener {

    private Bot bot;

    public PluginDispatch(Bot bot) {
        this.bot = bot;
    }

    @Override
    public boolean support(Event event) {
        return Listener.super.support(event);
    }

    @Override
    public void onListener(Event event) {

    }


    @Override
    public int getOrder() {
        return Listener.super.getOrder();
    }
}
