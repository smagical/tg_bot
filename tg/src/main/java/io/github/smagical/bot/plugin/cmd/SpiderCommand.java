package io.github.smagical.bot.plugin.cmd;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.cmd.SpiderCmdHandler;
import io.github.smagical.bot.lucene.LuceneFactory;
import io.github.smagical.bot.lucene.TgLucene;
import org.drinkless.tdlib.TdApi;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpiderCommand extends BaseCommand {
    private final ExecutorService executor =
            new ThreadPoolExecutor(
                    3,3,60, TimeUnit.SECONDS,
                    new LinkedBlockingDeque<>(), Executors.defaultThreadFactory()
            );
    final AtomicBoolean semaphore = new AtomicBoolean(false);
    final AtomicBoolean buildRun = new AtomicBoolean(false);
    public SpiderCommand(final Bot bot) {
        super(bot);
    }
    private  void addBuild() {
        synchronized (semaphore) {
            if (!semaphore.get()){
                semaphore.set(true);
                buildRun();
                return;
            }
        }
        synchronized (buildRun){
           if (buildRun.get()){
               return;
           }else {
               buildRun.set(true);
           }
        }
    }

    public void buildRun(){

        executor.execute(new Runnable() {
            @Override
            public void run() {
                buildRun.set(false);
                try {
                    TgLucene lucene = LuceneFactory.getTgLuceneByTgInfo();
                    lucene.buildIndex(-1);
                    lucene.close();
                } catch (SQLException e) {

                } catch (IOException e) {

                } catch (Exception e) {
                }
                synchronized (buildRun){
                    if (buildRun.get()) {
                        buildRun();
                    }else {
                        synchronized (semaphore){
                            semaphore.set(false);
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean solveCommand(String command, Long chatId, Long userId) {

        String[] tokens = command.split(" ");
        try {
            Long id = Long.parseLong(tokens[1]);
            TdApi.Chat chat = getBot().getChat(id);
            StringBuilder cmdStr = new StringBuilder();
            if (chat == null) {
                cmdStr.append("找不到Chat");
                senMessage(chatId,cmdStr.toString(),new ArrayList<>());
                return false;
            }

            executor.submit(new Runnable() {
                @Override
                public void run() {
                    senMessage(chatId,String.format("spider %d:%s 开始加载",chat.id,chat.title),new ArrayList<>());
                    SpiderCmdHandler.spider(
                            getBot(),id,0,-1,1000
                    );
                    addBuild();
                    senMessage(chatId,String.format("%d:%s 加载成功",chat.id,chat.title),new ArrayList<>());
                }
            });
            return true;
        }catch (NullPointerException e){
            return false;
        }

    }

    @Override
    public boolean supperCommand(String command) {
        return command.startsWith("spider") && command.split(" ").length == 2;
    }

    @Override
    public List<String> CommandList() {
        return List.of("spider chatId 爬虫");
    }
}
