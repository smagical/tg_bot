package io.github.smagical.bot;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;


@Slf4j
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws InterruptedException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //Pool pool = new Pool();

       // System.setProperty("java.library.path", Main.class.getResource("").getPath());

//        ReentrantLock lock = new ReentrantLock();
//        Condition condition = lock.newCondition();
//        new File("tddb").deleteOnExit();
//        String id= "999663"+ new Random().nextInt(1000,9999);
//        id = "9996610099";
        io.github.smagical.bot.bot.Bot bot = new io.github.smagical.bot.bot.Bot();
        bot.login();
//        lock.lock();
//        condition.await();





    }


}

