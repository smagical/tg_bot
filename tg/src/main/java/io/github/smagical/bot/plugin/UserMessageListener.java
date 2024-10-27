package io.github.smagical.bot.plugin;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.bot.handler.chat.update.message.NewMessageHandler;
import io.github.smagical.bot.listener.Listener;
import io.github.smagical.bot.lucene.LuceneFactory;
import io.github.smagical.bot.lucene.TgLucene;
import io.github.smagical.bot.plugin.cmd.*;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.github.smagical.bot.plugin.BotDb.END;
import static io.github.smagical.bot.plugin.cmd.InvCommand.ADD_PREFIX;

@Slf4j
public class UserMessageListener implements Listener<NewMessageHandler.MessageEvent> {
    private TgLucene tgLucene = null;
    private Bot bot = null;

    private final static long START_TIME = Instant.now().getEpochSecond();

    private final static int WORD_LEN = 30;

    private final List<Command> commands = new CopyOnWriteArrayList<>();

    public UserMessageListener(Bot bot) {
        try {
                tgLucene = LuceneFactory.getTgLuceneByTgInfo();
        } catch (IOException e) {}

        this.bot = bot;

        addCommand(new ListCommand(bot));
        addCommand(new AddCommand(bot));
        addCommand(new DelCommand(bot));
        addCommand(new InvCommand(bot));
        addCommand(new NoCommand(bot));
        addCommand(new SpiderCommand(bot));
    }

    public void addCommand(Command command) {
        commands.add(command);
        commands.sort((a,b)->Integer.compare(a.getOrder(),b.getOrder()));
    }
    public void removeCommand(Command command) {
        commands.remove(command);
    }

    @Override
    public void onListener(NewMessageHandler.MessageEvent event) {
       try {
           if (bot.getLoginType() == Bot.LoginType.BOT) return;
           TdApi.Message message = event.getData().getData();
           if (message.date < START_TIME) return;
           long chatId = message.chatId;

           log.debug(event.getData().getData().toString());

           if (message.content.getConstructor() == TdApi.MessageText.CONSTRUCTOR){
               TdApi.MessageText text = (TdApi.MessageText) message.content;
               if (text.text.text.endsWith(END)) return;
               if (text.text.text.startsWith(ADD_PREFIX)){
                   solveCommand(null,chatId,"inv_valid "+text.text.text);
                   return;
               }
               if (message.senderId.getConstructor() == TdApi.MessageSenderUser.CONSTRUCTOR){
                   long usrId = ((TdApi.MessageSenderUser)message.senderId).userId;
                   TdApi.User user = bot.getUser(usrId);
                   if (user != null) {
                       if (user.type.getConstructor() == TdApi.UserTypeBot.CONSTRUCTOR){
                           return;
                       }
                   }
                   TdApi.Chat chat = bot.getChat(chatId);
                   if (chat!=null && chat.type.getConstructor() == TdApi.ChatTypePrivate.CONSTRUCTOR){
                       solveCommand(usrId,chatId,text.text.text);
                       return;
                   }else {

                   }
               }else {
                   //return;
               }
               if (!BotDb.chatExist(chatId)){
                   return;
               }
               TgLucene.TgMessagePage query = tgLucene.queryMoreLikeThis(text.text.text.toUpperCase(Locale.ROOT), new TgLucene.TgPageHelper(100, 0));
               TdApi.FormattedText formattedText = getFromText(query.getMessages());

               TdApi.InputMessageText inputMessageText = new TdApi.InputMessageText(formattedText,new TdApi.LinkPreviewOptions(),true);

               TdApi.InputMessageReplyTo replyTo = getReplyTo(message.id);

               TdApi.SendMessage sendMessage = new TdApi.SendMessage(
                       chatId,0,replyTo,null,null,inputMessageText
               );
               bot.getClient()
                       .send(sendMessage,
                               new Client.ResultHandler() {
                                   @Override
                                   public void onResult(org.drinkless.tdlib.TdApi.Object object) {
                                       if (object.getConstructor() == org.drinkless.tdlib.TdApi.Ok.CONSTRUCTOR){

                                       }else {
                                           log.info(object.toString());
                                       }
                                   }
                               });

           }



       }catch (Exception e){
           e.printStackTrace();
       }

    }

    public void solveCommand(Long userId,Long chatId,String commandStr) {
        if (commandStr.equals("cmd")){
            cmdList(chatId);
        }else {
            for (Command command : commands) {
                if (command.supperCommand(commandStr)){
                    if (command.solveCommand(commandStr,chatId,userId));
                    break;
                }
            }
        }

    }
    private TdApi.FormattedText getFromText(List<TgLucene.TgMessage> messageList){
            StringBuilder textBuilder = new StringBuilder();
            List<TdApi.TextEntity> textEntities = new ArrayList<TdApi.TextEntity>();
            int index = 0;
            for (TgLucene.TgMessage tgMessage : messageList) {
                textBuilder.append(index +": ");
                String ms = tgMessage.getContent().substring(0,Math.min(WORD_LEN,tgMessage.getContent().length())).replace("\n","");
                textEntities.add(
                        new TdApi.TextEntity(
                                textBuilder.length(),
                                ms.length(),
                                new TdApi.TextEntityTypeTextUrl(
                                        tgMessage.getLink()
                                )
                        )
                );
                textBuilder.append(ms);
                textBuilder.append("\n");
                index++;
             }
            if (textBuilder.isEmpty()) textBuilder.append("未找到");
            TdApi.FormattedText formattedText = new TdApi.FormattedText(
                    textBuilder.append(END).toString(),
                    textEntities.toArray(new TdApi.TextEntity[textEntities.size()])
            );

            return formattedText;
    }
    private TdApi.InputMessageReplyToMessage getReplyTo(long messageId){
        return  new TdApi.InputMessageReplyToMessage(
                messageId ,null
        );
    }
    private void cmdList(Long chatId){
        StringBuilder cmdStr = new StringBuilder();
        for (Command command : commands) {
            for (String cmd:command.CommandList()){
                cmdStr.append(cmd);
                cmdStr.append("\n");
            }
        }
        org.drinkless.tdlib.TdApi.SendMessage sendMessage = new org.drinkless.tdlib.TdApi.SendMessage(
                chatId,0,null,null,null,
                new org.drinkless.tdlib.TdApi.InputMessageText(
                        new TdApi.FormattedText(cmdStr.append(END).toString(),null),
                        new org.drinkless.tdlib.TdApi.LinkPreviewOptions(),true)
        );

        bot.getClient()
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
