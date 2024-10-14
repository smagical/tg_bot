package io.github.smagical.bot.bot;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BotConfig {
    private String dbBase = "tddb";
    private AtomicInteger id = new AtomicInteger(0);
    private HashMap<String,String> idAndPath = new HashMap<>();
    private volatile static boolean flag = false;

    private BotConfig() {
        if (flag) throw new RuntimeException("Can't instantiate BotConfig");
        flag = true;
    }

    private final static BotConfig INSTANCE = new BotConfig();
    public static BotConfig getInstance() {
        return INSTANCE;
    }

    static {
        //TODO 处理多用户加载

    }

    public String getDbBase(String botId) {
        return dbBase +"/"+botId;
    }
    public void setId(int id) {
        this.id.set(id);
    }
    public void setDbBase(String dbBase) {
        this.dbBase = dbBase;
    }
    public int getId() {
        //TODO 处理多用户加载
        return 0;
    }
}
