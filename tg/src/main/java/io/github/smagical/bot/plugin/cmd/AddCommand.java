package io.github.smagical.bot.plugin.cmd;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.plugin.BotDb;
import org.drinkless.tdlib.TdApi;

import java.util.ArrayList;
import java.util.List;

public   class AddCommand extends BaseCommand {

    public AddCommand(Bot bot) {
        super(bot);
    }


    @Override
    public boolean solveCommand(String command, Long chatId, Long userId) {

        StringBuilder cmdBuilder = new StringBuilder();
        List< TdApi.TextEntity> entity = new ArrayList<>();
        String[] tokens = command.split(" ");
        try {
            Long id = Long.parseLong(tokens[1]);
            BotDb.addChatFrom(id, userId);
            cmdBuilder.append("添加成功");
        }catch (NumberFormatException e){
            return false;
        }
        senMessage(chatId,cmdBuilder.toString(),entity);
        return true;
    }

    @Override
    public boolean supperCommand(String command) {
        return command.startsWith("add") && (command.split(" ").length == 2);
    }

    @Override
    public List<String> CommandList() {
        return List.of("add chatId 添加");
    }
}