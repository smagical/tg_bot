package io.github.smagical.bot.plugin.cmd;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.plugin.BotDb;
import org.drinkless.tdlib.TdApi;

import java.util.ArrayList;
import java.util.List;

public class ListCommand extends BaseCommand {

    public ListCommand(Bot bot) {
        super(bot);
    }


    @Override
    public boolean solveCommand(String command, Long chatId, Long userId) {
        StringBuilder cmdBuilder = new StringBuilder();
        List< TdApi.TextEntity> entity = new ArrayList<>();
        List<Long> longs = BotDb.getChatFrom(userId);
        for (Long chatI : longs) {
            TdApi.Chat chat1 = getBot().getChat(chatI);
            if (chat1 == null){
                continue;
            }
            entity.add(
                    new TdApi.TextEntity(
                            cmdBuilder.length(),
                            Long.toString(chatI).length(),
                            new TdApi.TextEntityTypeCode()
                    )
            );
            cmdBuilder.append(chatI);
            cmdBuilder.append("-->");
            cmdBuilder.append(chat1.title);
            cmdBuilder.append("\n");
        }
        if (cmdBuilder.isEmpty()){
            cmdBuilder.append("没有队列");
        }
        senMessage(chatId,cmdBuilder.toString(),entity);
        return true;
    }

    @Override
    public boolean supperCommand(String command) {
        return command.startsWith("list");
    }

    @Override
    public List<String> CommandList() {
        return List.of("list 聊天列表");
    }
}