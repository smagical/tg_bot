package io.github.smagical.bot.plugin.cmd;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.plugin.BotDb;
import io.github.smagical.bot.plugin.TimeQueue;
import org.drinkless.tdlib.TdApi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public     class InvCommand extends BaseCommand {
    private final TimeQueue queue = new TimeQueue();
    public final static String ADD_PREFIX = "BotAdd_";
    public InvCommand(Bot bot) {
        super(bot);
    }


    @Override
    public boolean solveCommand(String command, Long chatId, Long userId) {
        if (command.startsWith("inv_valid")) {
            StringBuilder cmdBuilder = new StringBuilder();
            List< TdApi.TextEntity> entity = new ArrayList<>();
            String[] tokens = command.split(" ");
            String id = tokens[1];
            Long userId2 = queue.codeExists(id);
            if (userId2 == null) {
                cmdBuilder.append("未发现的token，请重新邀请");
            }else {
                BotDb.addChatFrom(chatId, userId2);
                cmdBuilder.append("添加成功");
            }
            senMessage(chatId,cmdBuilder.toString(),entity);

        }else {
            StringBuilder cmdBuilder = new StringBuilder();
            List< TdApi.TextEntity> entity = new ArrayList<>();
            cmdBuilder.append("random : ");
            String randomStr = ADD_PREFIX+ UUID.randomUUID()
                    .toString()
                    .replace("-","");
            queue.addCode(randomStr,userId);
            entity.add(
                    new TdApi.TextEntity(
                            cmdBuilder.length(),
                            randomStr.length(),
                            new TdApi.TextEntityTypeCode()
                    )
            );
            cmdBuilder.append(randomStr);
            senMessage(chatId,cmdBuilder.toString(),entity);
        }
        return true;
    }

    @Override
    public boolean supperCommand(String command) {
        return command.startsWith("inv") || (command.startsWith("inv_valid") && (command.split(" ").length == 2));
    }

    @Override
    public List<String> CommandList() {
        return List.of("inv 邀请码邀请");
    }

}