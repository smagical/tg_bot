package io.github.smagical.bot.lucene.analyzer;

import io.github.smagical.bot.hanlp.HanLp2;
import org.apache.lucene.analysis.Analyzer;

import java.util.HashSet;
import java.util.Set;

public class HanLP2Analyzer extends Analyzer implements AutoCloseable{

    private Set<String> filter = new HashSet<String>();
    private HanLp2 hanLp2;

    public HanLP2Analyzer(String pythonHome,String... filter) {

        for (String string : filter) {
            this.filter.add(string);
        }
        HanLp2.setPythonHome(pythonHome);
        hanLp2 = new HanLp2();
    }

    private HanLP2Analyzer(String pythonHome,String modePath,String... filter) {
        for (String string : filter) {
            this.filter.add(string);
        }
        HanLp2.setPythonHome(pythonHome);
        hanLp2 = new HanLp2(modePath);

    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        return new TokenStreamComponents(new HanLP2Tokenizer(hanLp2,this.filter));
    }

    @Override
    public void close() {
        super.close();
        try {
            hanLp2.close();
        } catch (Exception e) {
        }
    }
}
