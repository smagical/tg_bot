package io.github.smagical.bot.lucene.analyzer;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.lucene.HanLPTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;

import java.util.Set;

public class HanLPAnalyzer extends Analyzer {

    private boolean enablePorterStemming = false;
    private  Set<String> filter = null;
    private Segment tokenizer  = HanLP.newSegment();

    public HanLPAnalyzer(Segment tokenizer) {
        this.tokenizer = tokenizer;
    }

    public HanLPAnalyzer(boolean enablePorterStemming) {
        this.enablePorterStemming = enablePorterStemming;
    }

    public HanLPAnalyzer(boolean enablePorterStemming, Set<String> filter, Segment tokenizer) {
        this.enablePorterStemming = enablePorterStemming;
        this.filter = filter;
        this.tokenizer = tokenizer;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer hanLPTokenizer =new HanLPTokenizer(tokenizer, filter, enablePorterStemming);
        return new TokenStreamComponents(hanLPTokenizer);
    }
}
