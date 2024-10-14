package io.github.smagical.bot;

import io.github.smagical.bot.bot.Bot;
import lombok.extern.slf4j.Slf4j;
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
        lock.lock();
        condition.await();
        Example.main(args);




    }


}

