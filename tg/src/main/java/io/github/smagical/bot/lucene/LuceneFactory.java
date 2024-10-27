package io.github.smagical.bot.lucene;

import com.hankcs.hanlp.HanLP;
import io.github.smagical.bot.TgInfo;
import io.github.smagical.bot.lucene.analyzer.HanLP2Analyzer;
import io.github.smagical.bot.lucene.analyzer.HanLPAnalyzer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class LuceneFactory {

    public static void main(String[] args) throws Exception {

        TgLucene tgLucene = new TgLucene("base");
        for (TgLucene.TgMessage ienf : tgLucene.queryMoreLikeThis("IENF")) {
            System.out.println(ienf.toString());
        }
        tgLucene.close();
        tgLucene = new TgLucene("base_null",new HanLPAnalyzer(
                HanLP.newSegment()
        ));
        tgLucene.buildIndex(-1);
        for (TgLucene.TgMessage ienf : tgLucene.queryMoreLikeThis("IENF")) {
            System.out.println(ienf.toString());
        }
        tgLucene.close();
        tgLucene = new TgLucene("base_perceptron",new HanLPAnalyzer(
                HanLP.newSegment("perceptron")
        ));
        tgLucene.buildIndex(-1);
        for (TgLucene.TgMessage ienf : tgLucene.queryMoreLikeThis("IENF")) {
            System.out.println(ienf.toString());
        }
        tgLucene.close();

//        StringBuilder str = new StringBuilder("dasd");
//        byte[] bytes = new byte[]{(byte) 0xe2,(byte) 0x80, (byte) 0x8b};
//        str.append(new String(bytes, StandardCharsets.UTF_8));
//        System.out.println(str);
//        System.out.println(str.toString().endsWith(new String(bytes, StandardCharsets.UTF_8)));

    }

    public static TgLucene getTgLuceneByTgInfo() throws IOException {
        if (TgInfo.getPropertyInt(TgInfo.LUCENE_ANALYZER)==1){
            return new TgLucene(TgInfo.getProperty(TgInfo.LUCENE_DIR));
        }else
            return new TgLucene(TgInfo.getProperty(TgInfo.LUCENE_DIR),new HanLP2Analyzer(TgInfo.getProperty(TgInfo.PYTHONHOME)));
    }

}
