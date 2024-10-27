package io.github.smagical.bot;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;


@Slf4j
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static void main(String[] args) throws IOException {

        io.github.smagical.bot.bot.Bot bot = new io.github.smagical.bot.bot.Bot();
        if (TgInfo.getPropertyInt(TgInfo.LOGIN_TYPE) == 0) {
            bot.loginByPthone(TgInfo.getProperty(TgInfo.LOGIN_NUMBER));
        } else if (TgInfo.getPropertyInt(TgInfo.LOGIN_TYPE) == 1) {
            bot.loginByOcr();
        } else
            bot.loginByBotToken(TgInfo.getProperty(TgInfo.LOGIN_TOKEN));


    }


}

