package io.github.smagical.bot.plugin.cmd;

import io.github.smagical.bot.bot.Bot;

import java.util.List;

public    class NoCommand extends BaseCommand {
    public NoCommand(Bot bot) {
        super(bot);
    }


    @Override
    public boolean solveCommand(String command, Long chatId, Long userId) {
        StringBuilder cmdBuilder = new StringBuilder();
        cmdBuilder.append("命令不正确\n");
        cmdBuilder.append("回复cmd查看命令\n");
        senMessage(chatId,cmdBuilder.toString(),null);
        return true;
    }

    @Override
    public boolean supperCommand(String command) {
        return true;
    }

    @Override
    public List<String> CommandList() {
        return List.of();
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}