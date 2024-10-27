package io.github.smagical.bot.cmd;

import io.github.smagical.bot.TgInfo;
import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.lucene.TgLucene;
import io.github.smagical.bot.lucene.analyzer.HanLP2Analyzer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

import static io.github.smagical.bot.cmd.MainCmd.printSplit;
import static io.github.smagical.bot.cmd.MainCmd.select;

@Slf4j
public class QueryCmdHandler implements Cmd{
    private final static QueryCmdHandler instance = new QueryCmdHandler();
    public static QueryCmdHandler getInstance() {
        return instance;
    }

    @Override
    public void printMainMenu(Bot bot) {
        TgLucene tgLucene = null;
        if (TgInfo.getPropertyInt(TgInfo.LUCENE_ANALYZER) == 1){
            try {
                tgLucene = new TgLucene(TgInfo.getProperty(TgInfo.LUCENE_DIR));
            } catch (IOException e) {
            }
        }else {
            tgLucene = new TgLucene(TgInfo.getProperty(TgInfo.LUCENE_DIR),new HanLP2Analyzer(TgInfo.getProperty(TgInfo.PYTHONHOME)));
        }
        while (true){
            printSplit();
            log.info("############### QueryCmdHandler ####################");
            log.info("############### q text  limit ####################");
            log.info("############### qx text  limit  #moreLikeQuery####################");
            log.info("############### ext back to Main menu ####################");
            printSplit();
            String select = select("请选择");
            while (true){
                if (select.startsWith("q")){
                    String args[] = select.split(" ");
                    if (args.length != 4){
                        continue;
                    }
                    int linmt = -1;
                   try{
                       linmt = Integer.parseInt(args[2]);
                       List<TgLucene.TgMessage> messages = null;
                       if (select.endsWith("qx")){
                           messages = tgLucene.queryMoreLikeThis(args[1],null,linmt);
                       }else {
                           messages = tgLucene.query(args[1],null,linmt);
                       }
                       for (TgLucene.TgMessage message : messages) {
                           log.info(message.toString());
                       }
                   }catch (Exception e){}
                    break;

                }else if (select.startsWith("ext")){
                    try {
                        if (tgLucene != null)
                            tgLucene.close();
                    } catch (Exception e) {

                    }
                    return;
                }
            }
        }
    }
}
