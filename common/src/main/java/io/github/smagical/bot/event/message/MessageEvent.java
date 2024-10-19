package io.github.smagical.bot.event.message;

import io.github.smagical.bot.event.BaseEvent;

public  class MessageEvent<T> extends BaseEvent<T> {
    public MessageEvent(T code) {
        super(code);
    }
}
