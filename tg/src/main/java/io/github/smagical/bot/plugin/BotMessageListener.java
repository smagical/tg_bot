package io.github.smagical.bot.plugin;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.bot.handler.chat.update.bot.NewCallbackQueryHandler;
import io.github.smagical.bot.bot.handler.chat.update.message.NewMessageHandler;
import io.github.smagical.bot.bot.util.ClientUtils;
import io.github.smagical.bot.event.Event;
import io.github.smagical.bot.listener.Listener;
import io.github.smagical.bot.lucene.LuceneFactory;
import io.github.smagical.bot.lucene.TgLucene;
import io.github.smagical.bot.plugin.cmd.*;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.github.smagical.bot.plugin.BotDb.END;
import static io.github.smagical.bot.plugin.cmd.InvCommand.ADD_PREFIX;

@Slf4j
public class BotMessageListener implements Listener {
    private TgLucene tgLucene = null;
    private Bot bot = null;

    private final static long START_TIME = Instant.now().getEpochSecond();
    private final static int WORD_LEN = 30;
    private final static JsonMapper JSON_MAPPER = JsonMapper
            .builder()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .addModule(new JavaTimeModule())
            .build();
    private final static int PAGE_SIZE = 10;
    private final List<Command> commands = new CopyOnWriteArrayList<>();
    private final List<Class> supportClass = Arrays.asList(
            NewMessageHandler.MessageEvent.class,
            NewCallbackQueryHandler.NewCallbackQueryEvent.class
    );



    public BotMessageListener(Bot bot) {
        try {
            tgLucene = LuceneFactory.getTgLuceneByTgInfo();
        } catch (IOException e) {

        }
        this.bot = bot;
        addCommand(new ListCommand(bot));
        addCommand(new AddCommand(bot));
        addCommand(new DelCommand(bot));
        addCommand(new InvCommand(bot));
        addCommand(new NoCommand(bot));
        addCommand(new SpiderCommand(bot));
        initCommand();
    }

    public void addCommand(Command command) {
        commands.add(command);
        commands.sort((a,b)->Integer.compare(a.getOrder(),b.getOrder()));
    }
    public void removeCommand(Command command) {
        commands.remove(command);
    }
    public void initCommand(){
        HashMap<String,String> map = new HashMap<>();
        for (Command cmd : commands) {
            for (String string : cmd.CommandList()) {
                String cmdStr = string.strip().split(" ")[0];
                map.put(cmdStr,string.strip());
            }
        }
        ClientUtils.setCommand(
                bot.getClient(),map,new TdApi.BotCommandScopeAllPrivateChats()
        );
    }

    @Override
    public void onListener(Event event) {
        if (NewMessageHandler.MessageEvent.class.isAssignableFrom(event.getClass())) {
            onMessageEventListener((NewMessageHandler.MessageEvent) event);
        }else if (NewCallbackQueryHandler.NewCallbackQueryEvent.class.isAssignableFrom(event.getClass())) {
            onNewCallbackQueryEventListener((NewCallbackQueryHandler.NewCallbackQueryEvent) event);
        }
    }


    private void onNewCallbackQueryEventListener(NewCallbackQueryHandler.NewCallbackQueryEvent event) {
         TdApi.UpdateNewCallbackQuery callbackQuery = event.getData().getData();
         if (callbackQuery.payload.getConstructor() != TdApi.CallbackQueryPayloadData.CONSTRUCTOR){
             return;
         }
        try {
            byte[] data = ((TdApi.CallbackQueryPayloadData)callbackQuery.payload).data;
            PageInfo info = JSON_MAPPER.readValue(data,PageInfo.class);
            if (info.page == info.now) return;
            updateMessage(callbackQuery.messageId,callbackQuery.chatId,info.text,info.page, info.getPageSize());

        } catch (IOException e) {
            //throw new RuntimeException(e);
        }

    }

    private void onMessageEventListener(NewMessageHandler.MessageEvent event) {
        try {
            if (bot.getLoginType() != Bot.LoginType.BOT) return;
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
                        String cmd = text.text.text;
                        if (cmd.startsWith("/")) cmd = cmd.substring(1);
                        solveCommand(usrId,chatId,cmd);
                        return;
                    }else {
                        if (usrId == bot.getMe().id){
                            return;
                        }
                    }
                }else {
                    //return;
                }

                if (!BotDb.chatExist(chatId)) return;
                sendMessage(message.id,chatId,text.text.text,0,PAGE_SIZE);
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
    private TdApi.ReplyMarkup getReplyMarkup(String text,TgLucene.TgMessagePage page) throws JsonProcessingException {
        ArrayList<TdApi.InlineKeyboardButton> button = new ArrayList<>();
        int lastPage = page.getTotal() / PAGE_SIZE;
        PageInfo info = new PageInfo(text,page.getPage(), 0, PAGE_SIZE);
        button.add(new TdApi.InlineKeyboardButton(
                    "首页",
                    new TdApi.InlineKeyboardButtonTypeCallback(
                            JSON_MAPPER.writeValueAsBytes(info)
                    )
        ));


        info = new PageInfo(text,page.getPage(), page.getPage()-1, PAGE_SIZE);
        if (info.page >= 0){
            button.add(new TdApi.InlineKeyboardButton(
                    "上一页",
                    new TdApi.InlineKeyboardButtonTypeCallback(
                            JSON_MAPPER.writeValueAsBytes(info)
                    )
            ));
        }

        info = new PageInfo(text,page.getPage(), page.getPage(), PAGE_SIZE);
        button.add(new TdApi.InlineKeyboardButton(
                String.format("%d/%d",page.getPage(),lastPage),
                new TdApi.InlineKeyboardButtonTypeCallback(
                        JSON_MAPPER.writeValueAsBytes(info)
                )
            ));


        info = new PageInfo(text,page.getPage(), page.getPage()+1, PAGE_SIZE);
        if (info.page <= lastPage){
            button.add(new TdApi.InlineKeyboardButton(
                    "下一页",
                    new TdApi.InlineKeyboardButtonTypeCallback(
                            JSON_MAPPER.writeValueAsBytes(info)
                    )
            ));
        }

        info = new PageInfo(text,page.getPage(), lastPage, PAGE_SIZE);
        button.add(new TdApi.InlineKeyboardButton("尾页",
                new TdApi.InlineKeyboardButtonTypeCallback(
                        JSON_MAPPER.writeValueAsBytes(info)
                )
            ));
        TdApi.ReplyMarkup replyMarkup = new TdApi.ReplyMarkupInlineKeyboard(
                new TdApi.InlineKeyboardButton[][]{
                        button.toArray(TdApi.InlineKeyboardButton[]::new)
                }
        );
        return replyMarkup;
    }
    private void sendMessage(Long message,Long chatId,String text,int page,int pageSize) throws IOException {
        TgLucene.TgMessagePage query = tgLucene.queryMoreLikeThis(text.toUpperCase(Locale.ROOT), new TgLucene.TgPageHelper(pageSize, page));
        TdApi.FormattedText formattedText = getFromText(query.getMessages());

        TdApi.InputMessageText inputMessageText = new TdApi.InputMessageText(formattedText,new TdApi.LinkPreviewOptions(),true);

        TdApi.InputMessageReplyTo replyTo = getReplyTo(message);

        TdApi.ReplyMarkup replyMarkup = getReplyMarkup(text,query);

        TdApi.SendMessage sendMessage = new TdApi.SendMessage(
                chatId,0,replyTo,null,replyMarkup,inputMessageText
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
    private void updateMessage(Long message,Long chatId,String text,int page,int pageSize) throws IOException {
        TgLucene.TgMessagePage query = tgLucene.queryMoreLikeThis(text.toUpperCase(Locale.ROOT), new TgLucene.TgPageHelper(pageSize, page));
        TdApi.FormattedText formattedText = getFromText(query.getMessages());

        TdApi.InputMessageText inputMessageText = new TdApi.InputMessageText(formattedText,new TdApi.LinkPreviewOptions(),true);

        TdApi.InputMessageReplyTo replyTo = getReplyTo(message);

        TdApi.ReplyMarkup replyMarkup = getReplyMarkup(text,query);

        TdApi.EditMessageText editMessageText = new TdApi.EditMessageText(
                chatId,message,replyMarkup,inputMessageText
        );
        bot.getClient()
                .send(editMessageText,
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

    @Override
    public boolean support(Event event) {
        if (event == null) return false;
        for (Class aClass : supportClass) {
            if (aClass.isAssignableFrom(event.getClass())) {
                return true;
            }
        }
        return false;
    }

    private static class PageInfo{
        private String text;
        private int now;
        private int page;
        private int pageSize;

        public PageInfo() {
        }

        public PageInfo(String text, int now, int page, int pageSize) {
            this.text = text;
            this.now = now;
            this.page = page;
            this.pageSize = pageSize;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getNow() {
            return now;
        }

        public void setNow(int now) {
            this.now = now;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }
    }
}
