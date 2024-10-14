package io.github.smagical.bot;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.bot.HandlerWrapper;
import io.github.smagical.bot.bot.listener.user.authorization.state.LoginListener;
import io.github.smagical.bot.event.user.LoginEvent;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.example.Example;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


@Slf4j
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws InterruptedException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //Pool pool = new Pool();

       // System.setProperty("java.library.path", Main.class.getResource("").getPath());

        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        new File("tddb").deleteOnExit();
        String id= "999663"+ new Random().nextInt(1000,9999);
        id = "9996610099";
        Bot bot = new Bot();
        bot.login();
        bot.addListener(
                new LoginListener(bot){
                    @Override
                    public void onListener(LoginEvent loginEvent) {
                        if (loginEvent instanceof LoginEvent.LoginSuccessEvent) {
                            bot.getClient()
                                    .send(
                                            new TdApi.LoadChats(
                                                    new TdApi.ChatListMain(), 1000
                                            ),
                                            new HandlerWrapper() {
                                                @Override
                                                public Bot getBot() {
                                                    return bot;
                                                }

                                                @Override
                                                public void onResult(TdApi.Object object) {
                                                  log.info(object.toString());
                                                }
                                            }
                                    );
                        }
                    }
                }
        );
        lock.lock();
        condition.await();
        Example.main(args);




    }


}

