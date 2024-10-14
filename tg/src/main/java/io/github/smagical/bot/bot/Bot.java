package io.github.smagical.bot.bot;

import io.github.smagical.bot.bot.handler.DispatchHandler;
import io.github.smagical.bot.bot.handler.LogHandler;
import io.github.smagical.bot.bot.handler.UpdateOptionHandler;
import io.github.smagical.bot.bot.handler.chat.ChatDispatchHandler;
import io.github.smagical.bot.bot.handler.chat.ChatLoadListener;
import io.github.smagical.bot.bot.handler.user.update.UpdateUserDispatchHandler;
import io.github.smagical.bot.bot.handler.user.authorization.state.AuthorizationStateDispatchHandler;
import io.github.smagical.bot.bot.listener.user.UserListener;
import io.github.smagical.bot.bot.listener.user.authorization.state.AuthorizationStateListener;
import io.github.smagical.bot.bot.listener.user.chat.LoginForChatInitListener;
import io.github.smagical.bot.bus.MessageDispatch;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import org.jetbrains.annotations.NotNull;

import java.io.IOError;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class Bot extends MessageDispatch implements io.github.smagical.bot.Bot {

    private String id;
    private Client client;
    private DispatchHandler dispatchHandler;
    private LoginType loginType = LoginType.PHONE_NUMBER;
    private ChatMap chatMap = new ChatMap();




    private String phoneNumber;
    public static enum LoginType {
        PHONE_NUMBER, OCR
    }

    public Bot() {
        this.id = String.valueOf(BotConfig.getInstance()
                .getId());
    }

    public void login() {
        login(LoginType.OCR);
    }

    public void login(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.id = phoneNumber;
        login(LoginType.PHONE_NUMBER);
    }

    private void login(LoginType loginType) {
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

        this.client = Client.create(this.dispatchHandler, LogHandler.getInstance(), null);
        initHandler();
        initListener();

    }

    private void initHandler(){
        this.dispatchHandler.addHandler(new AuthorizationStateDispatchHandler(this));
        this.dispatchHandler.addHandler(new UpdateOptionHandler());
        this.dispatchHandler.addHandler(new UpdateUserDispatchHandler(this));

        this.dispatchHandler.addHandler(new ChatDispatchHandler(this));
    }

    private void initListener(){


        addListener(new AuthorizationStateListener(this));
        addListener(new UserListener());



        addListener(new ChatLoadListener(this));
        addListener(new LoginForChatInitListener(this));
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

    public boolean add(TdApi.Object chat) {
        Long id = null;
        if (chat instanceof TdApi.SecretChat) {
            id = (long) ((TdApi.SecretChat)chat).id;
        }else {
            id = ((TdApi.Chat)chat).id;
        }
        chatMap.put(id, chat);
        return true;
    }

    public boolean addChat(TdApi.Chat chat) {
        chatMap.put(chat.id, chat);
        return true;
    }

    public boolean addSecretChat(TdApi.SecretChat secretChat) {
        chatMap.put((long) secretChat.id,secretChat);
        return true;
    }

    public TdApi.Chat getChat(long id) {
        return chatMap.getChat(id);
    }

    public TdApi.SecretChat getSecretChat(long id) {
        return chatMap.getSecretChat(id);
    }

    public TdApi.Object get(long id){
        return chatMap.get(id);
    }

    public boolean remove(long id) {
        return chatMap.remove(id);
    }

    private class ChatMap {
        private static final long serialVersionUID = 1L;
        private ConcurrentHashMap<Long, TdApi.SecretChat> secretChatMap = new ConcurrentHashMap<>();
        private ConcurrentHashMap<Long, TdApi.Chat> chatMap = new ConcurrentHashMap<>();

        public TdApi.Object put(@NotNull Long key, @NotNull TdApi.Object value) {
            if (value instanceof TdApi.SecretChat) {
                return secretChatMap.put(key, (TdApi.SecretChat) value);
            }else {
                return chatMap.put(key, (TdApi.Chat) value);
            }

        }

        public TdApi.Object putIfAbsent(Long key, TdApi.Object value) {
            if (value instanceof TdApi.SecretChat) {
                return secretChatMap.putIfAbsent(key, (TdApi.SecretChat) value);
            }else {
                return chatMap.putIfAbsent(key, (TdApi.Chat) value);
            }
        }

        public TdApi.Object get(Long key) {
            TdApi.Object result =  chatMap.get(key);
            if (result == null) {
                result = secretChatMap.get(key);
            }
            return result;
        }

        public TdApi.Chat getChat(Long key) {
            return chatMap.get(key);
        }

        public TdApi.SecretChat getSecretChat(Long key) {
            return secretChatMap.get(key);
        }

        public boolean remove(long id) {
            if (chatMap.remove(id) != null) {
                return true;
            }else if (secretChatMap.remove(id) != null) {
               return true;
            }
            return false;
        }
    }

}


