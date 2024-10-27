package io.github.smagical.bot.plugin;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.bot.handler.user.authorization.state.AuthorizationStateDispatchHandler;
import io.github.smagical.bot.listener.Listener;

public class PluginInitListener  implements Listener<AuthorizationStateDispatchHandler.LoginSuccessEvent> {

    private Bot bot;

    public PluginInitListener(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onListener(AuthorizationStateDispatchHandler.LoginSuccessEvent event) {
        bot.removeListener(this);
        bot.addListener(new UserMessageListener(bot));
        bot.addListener(new BotMessageListener(bot));
    }
}
