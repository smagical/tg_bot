package io.github.smagical.bot.bot.listener.user;

import io.github.smagical.bot.bot.handler.user.update.UpdateUserDispatchHandler;
import io.github.smagical.bot.listener.Listener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserListener<T,V> implements Listener<UpdateUserDispatchHandler.UpdateUserEvent> {

    @Override
    public void onListener(UpdateUserDispatchHandler.UpdateUserEvent event) {
        log.info("UserListener received update user event: {}", event.getOriginalData());
    }
}
