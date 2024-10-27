package io.github.smagical.bot.plugin.cmd;

import java.util.List;

public   interface Command{
    default int getOrder() {
        return Integer.MAX_VALUE - 10;
    };
    boolean solveCommand(String command,Long chatId,Long userId);
    boolean supperCommand(String command);
    List<String> CommandList();

}