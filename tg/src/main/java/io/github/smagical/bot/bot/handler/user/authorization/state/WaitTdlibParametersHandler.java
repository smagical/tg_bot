package io.github.smagical.bot.bot.handler.user.authorization.state;

import io.github.smagical.bot.TgInfo;
import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.bot.BotConfig;
import io.github.smagical.bot.bot.handler.base.RetryBaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

import java.io.File;
import java.nio.charset.StandardCharsets;

@Slf4j
public class WaitTdlibParametersHandler extends RetryBaseHandlerWrapper {


    public WaitTdlibParametersHandler(Bot bot) {
        super(bot);
    }

    @Override
    public int[] support() {
        return new int[]{TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR};
    }

    @Override
    protected void error(TdApi.Error error) {
        getBot().send(
                new AuthorizationStateDispatchHandler.LoginFailureEvent(error)
        );
    }

    @Override
    public void handle(TdApi.Object object) {
        TdApi.SetTdlibParameters request = new TdApi.SetTdlibParameters();
        request.apiId = TgInfo.getPropertyInt(TgInfo.API_ID);
        request.apiHash = TgInfo.getProperty(TgInfo.API_HASH);
        request.applicationVersion = TgInfo.getProperty(TgInfo.APPLICATION_VERSION);
        request.deviceModel = System.getProperty("os.name","未知");
        request.systemLanguageCode = TgInfo.getProperty(TgInfo.LANGUAGE_CODE);
        request.useTestDc = TgInfo.getPropertyBool(TgInfo.USE_TEST);
        request.useChatInfoDatabase = true;
        request.useFileDatabase = true;
        request.useSecretChats = true;
        request.databaseDirectory = BotConfig.getInstance().getDbBase(getBot().getBotId());
        File file = new File(request.databaseDirectory);
        if (file.isFile()) {
            file.delete();
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        request.databaseEncryptionKey = TgInfo.getProperty(TgInfo.DATABASE_ENCRYPTION_KEY).getBytes(StandardCharsets.UTF_8);
        getBot().getClient().send(request,this::onResult);

    }

}
