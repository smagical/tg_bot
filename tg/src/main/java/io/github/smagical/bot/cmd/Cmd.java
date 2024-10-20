package io.github.smagical.bot.cmd;

import io.github.smagical.bot.bot.Bot;

public interface Cmd {
    public void printMainMenu(Bot bot);
    default String getName(){
        return this.getClass().getSimpleName();
    };
}
