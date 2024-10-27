package io.github.smagical.bot.plugin.cmd;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.bot.util.ClientUtils;
import io.github.smagical.bot.plugin.BotDb;
import org.drinkless.tdlib.TdApi;

import java.util.ArrayList;
import java.util.List;

public   class DelCommand extends BaseCommand {

    public DelCommand(Bot bot) {
        super(bot);
    }


    @Override
    public boolean solveCommand(String command, Long chatId, Long userId) {

        StringBuilder cmdBuilder = new StringBuilder();
        List< TdApi.TextEntity> entity = new ArrayList<>();
        String[] tokens = command.split(" ");
        if (tokens.length != 2) {
            cmdBuilder.append("不支持的命令");
        }else {
            try {
                Long id = Long.parseLong(tokens[1]);
                if (BotDb.delChatFrom(id, userId)){
                    if (!BotDb.chatExist(id)){
                        ClientUtils.LeaveChat(
                                getBot().getClient(),id
                        );
                    }
                }
                cmdBuilder.append("删除成功");

            }catch (NumberFormatException e){
                return false;
            }
        }
        senMessage(chatId,cmdBuilder.toString(),entity);
        return true;
    }

    @Override
    public boolean supperCommand(String command) {
        return command.startsWith("del") && (command.split(" ").length == 2);
    }

    @Override
    public List<String> CommandList() {
        return List.of("del chatId 删除");
    }
}