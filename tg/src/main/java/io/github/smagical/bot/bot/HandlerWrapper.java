package io.github.smagical.bot.bot;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

public interface HandlerWrapper extends Client.ResultHandler {
     default int[] support(){
          return new int[0];
     }
     default void onHandle(TdApi.Object object) {
          onResult(object);
     }
     Bot getBot();

     default int getOrder(){
          return Integer.MAX_VALUE;
     }
}
