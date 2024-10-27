package io.github.smagical.bot.plugin.cmd;

import io.github.smagical.bot.bot.Bot;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import java.util.List;

import static io.github.smagical.bot.plugin.BotDb.END;

@Slf4j
public  abstract class BaseCommand implements Command{
    private Bot bot;

    public BaseCommand(Bot bot) {
        this.bot = bot;
    }

    protected  Bot getBot(){
        return bot;
    };
    protected void senMessage(long chatId, String text, List<TdApi.TextEntity> entities){
        senMessage(chatId,
                new TdApi.FormattedText(text,entities==null?null:entities.toArray(new TdApi.TextEntity[entities.size()])));
    }
    protected void senMessage(long chatId, org.drinkless.tdlib.TdApi.FormattedText formattedText){
        formattedText.text = formattedText.text + END;
        org.drinkless.tdlib.TdApi.SendMessage sendMessage = new org.drinkless.tdlib.TdApi.SendMessage(
                chatId,0,null,null,null,
                new org.drinkless.tdlib.TdApi.InputMessageText(formattedText,new org.drinkless.tdlib.TdApi.LinkPreviewOptions(),true)
        );

        getBot().getClient()
                .send(sendMessage, new Client.ResultHandler() {
                    @Override
                    public void onResult(org.drinkless.tdlib.TdApi.Object object) {
                        if (object.getConstructor() == org.drinkless.tdlib.TdApi.Ok.CONSTRUCTOR){

                        }else {
                            log.info(object.toString());
                        }
                    }
                });
    }
}