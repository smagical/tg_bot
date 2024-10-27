package io.github.smagical.bot.bot.handler.user.authorization.state;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.bot.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public  class WaitPhoneNumberHandler extends BaseHandlerWrapper  {

    public WaitPhoneNumberHandler(Bot bot) {
        super(bot);
    }

    @Override
    public int[] support() {
        return new int[]{TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR};
    }


    @Override
    protected void onError(TdApi.Error error) {
        super.onError(error);
        getBot().send(
                new AuthorizationStateDispatchHandler.LoginFailureEvent(error)
        );
    }

    @Override
    protected void handle(TdApi.Object object) {
        if (getBot().getLoginType() == Bot.LoginType.OCR){
            getBot().getClient().send(new TdApi.RequestQrCodeAuthentication(),this);
        } else {
            TdApi.PhoneNumberAuthenticationSettings settings =
                    new TdApi.PhoneNumberAuthenticationSettings();
            settings.hasUnknownPhoneNumber  = false;
            settings.allowFlashCall = false;
            settings.allowMissedCall = false;
            settings.isCurrentPhoneNumber = true;

            getBot().getClient()
                    .send(new TdApi.SetAuthenticationPhoneNumber(getBot().getPhoneNumber(), settings),this);

        }
    }


}
