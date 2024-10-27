package io.github.smagical.bot.bot;

import io.github.smagical.bot.bot.handler.DispatchHandler;
import io.github.smagical.bot.bot.handler.LogHandler;
import io.github.smagical.bot.bot.handler.UpdateOptionHandler;
import io.github.smagical.bot.bot.handler.chat.ChatDispatchHandler;
import io.github.smagical.bot.bot.handler.chat.ChatLoadListener;
import io.github.smagical.bot.bot.handler.user.authorization.state.AuthorizationStateDispatchHandler;
import io.github.smagical.bot.bot.handler.user.update.UpdateUserDispatchHandler;
import io.github.smagical.bot.bot.listener.user.UserListener;
import io.github.smagical.bot.bot.listener.user.authorization.state.AuthorizationStateListener;
import io.github.smagical.bot.bot.listener.user.chat.LoginForChatInitListener;
import io.github.smagical.bot.bot.listener.user.chat.message.MessageDispatchListener;
import io.github.smagical.bot.bot.model.ChatMap;
import io.github.smagical.bot.bot.model.UserMap;
import io.github.smagical.bot.bot.util.ClientUtils;
import io.github.smagical.bot.bus.MessageDispatch;
import io.github.smagical.bot.cmd.MainCmd;
import io.github.smagical.bot.event.user.LoginEvent;
import io.github.smagical.bot.listener.Listener;
import io.github.smagical.bot.plugin.PluginInitListener;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import java.io.IOError;
import java.io.IOException;

public class Bot extends MessageDispatch implements io.github.smagical.bot.Bot {

    private String id;
    private Client client;
    private DispatchHandler dispatchHandler;
    private LoginType loginType = LoginType.PHONE_NUMBER;
    private ChatMap chatMap = new ChatMap();
    private UserMap users = new UserMap();
    private volatile Boolean isRunning = false;
    private volatile TdApi.User me = null;


    private String phoneNumber;
    private String botToken;

    public String getBotToken() {
        return botToken;
    }

    public static enum LoginType {
        PHONE_NUMBER, OCR,BOT;
    }

    public Bot() {
        this.id = String.valueOf(BotConfig.getInstance()
                .getId());
    }

    public void loginByOcr() {
        checkRunning();
        login(LoginType.OCR);
    }

    public void loginByPthone(String phoneNumber) {
        checkRunning();
        synchronized (isRunning){
            this.phoneNumber = phoneNumber;
            this.id = phoneNumber;
            login(LoginType.PHONE_NUMBER);
        }
    }
    public void loginByBotToken(String botToken) {
        checkRunning();
        synchronized (isRunning){
            this.botToken = botToken;
            this.id = botToken.replace(":","_");
            login(LoginType.BOT);
        }
    }

    private void login(LoginType loginType) {
        checkRunning();
        synchronized (isRunning) {
            this.loginType = loginType;
            this.dispatchHandler = new DispatchHandler(this);
            Client.setLogMessageHandler(0, LogHandler.getInstance());
            // disable TDLib log and redirect fatal errors and plain log messages to a file
            try {
                Client.execute(new TdApi.SetLogVerbosityLevel(0));
                Client.execute(new TdApi.SetLogStream(new TdApi.LogStreamFile("tdlib.log", 1 << 27, false)));
            } catch (Client.ExecutionException error) {
                throw new IOError(new IOException("Write access to the current directory is required"));
            }
            initHandler();
            initListener();
            this.client = Client.create(this.dispatchHandler, LogHandler.getInstance(), null);
            isRunning = true;
            BotConfig.getInstance().initThreadExecutor();
        }


    }

    public void logout(){
        synchronized (isRunning) {
            if (!isRunning) return;
            this.isRunning = false;
            BotConfig.getInstance().stopThreadExecutor();
        }

    }

    private void initHandler(){
        this.dispatchHandler.addHandler(new AuthorizationStateDispatchHandler(this));
        this.dispatchHandler.addHandler(new UpdateOptionHandler());
        this.dispatchHandler.addHandler(new UpdateUserDispatchHandler(this));

        this.dispatchHandler.addHandler(new ChatDispatchHandler(this));
    }

    private void initListener(){


        addListener(new AuthorizationStateListener(this));
        addListener(new UserListener(this));

        addListener(new ChatLoadListener(this));
        addListener(new LoginForChatInitListener(this));
        addListener(new MessageDispatchListener(this));

        addListener(new GetMeHandler());

        addListener(new PluginInitListener(this));
        addListener(new MainCmd.CmdListener(this));
    }



    public Client getClient() {
        return client;
    }

    public String getBotId() {
        return id;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public LoginType getLoginType() {
        return this.loginType;
    }


    public boolean addChat(TdApi.Chat chat) {
        chatMap.put(chat.id, chat);
        return true;
    }

    public boolean addSecretChat(TdApi.SecretChat secretChat) {
        chatMap.put((long) secretChat.id,secretChat);
        return true;
    }


    public TdApi.Chat getChat(long chatId) {
        TdApi.Chat chat = chatMap.getChat(chatId);
        if (chat == null){
            try {
                chat = ClientUtils.getChat(getClient(),chatId);
                if (chat != null)
                   addChat(chat);
            } catch (InterruptedException e) {
            }
        }
        return chat;
    }

    public TdApi.SecretChat getSecretChat(long id) {
        return chatMap.getSecretChat(id);
    }

    public boolean removeChat(long id) {
        return chatMap.remove(id);
    }

    public ChatMap getAllChats() {
        return chatMap;
    }

    public TdApi.User getUser(long userId) {
        TdApi.User user = users.getUser(userId);
        if (user == null){
            try {
                user = ClientUtils.getUser(getClient(),userId);
                if (user != null)
                    addUser(user);
            } catch (InterruptedException e) {
            }
        }
        return user;
    }

    public TdApi.UserFullInfo getUserFullInfo(long id) {
        return users.getFullInfo(id);
    }
    public UserMap.Entity getUserEntity(long id) {
        return users.getEntity(id);
    }
    public void addUser(TdApi.User user) {
        users.put(user);
    }
    public void addUserFullInfo(Long userId,TdApi.UserFullInfo userFullInfo) {
        users.put(userId,userFullInfo);
    }
    public boolean removeUser(long id) {
        return users.remove(id);
    }

    public UserMap getAllUser() {
        return users;
    }

    public TdApi.User getMe(){
        return me;
    }
    private void checkRunning() {
        synchronized (isRunning){
            if (isRunning) {
                throw new IllegalStateException("Bot is already running");
            }
        }
    }




    private class GetMeHandler implements Listener<LoginEvent.LoginSuccessEvent> , Client.ResultHandler{
        private int retryCount = 3;
        public GetMeHandler() {
        }

        @Override
        public void onResult(TdApi.Object object) {
            if (object.getConstructor() == TdApi.User.CONSTRUCTOR)
                Bot.this.me = (TdApi.User) object;
            else if (retryCount > 0){
                retryCount--;
                onListener(null);
            }

        }


        @Override
        public void onListener(LoginEvent.LoginSuccessEvent event) {
            getClient()
                    .send(
                            new TdApi.GetMe(),this
                    );
        }
    }

}


