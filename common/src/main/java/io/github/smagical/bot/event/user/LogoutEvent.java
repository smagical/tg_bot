package io.github.smagical.bot.event.user;

import io.github.smagical.bot.event.BaseEvent;

public class LogoutEvent<T> extends BaseEvent<T> {
    public LogoutEvent(T code) {
        super(code);
    }
}
