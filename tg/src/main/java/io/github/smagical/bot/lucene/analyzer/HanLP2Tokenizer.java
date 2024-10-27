package io.github.smagical.bot.lucene.analyzer;

import com.hankcs.hanlp.utility.TextUtility;
import io.github.smagical.bot.hanlp.HanLp2;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Set;

class HanLP2Tokenizer extends Tokenizer {
    // 当前词
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    // 偏移量
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    // 距离
    private final PositionIncrementAttribute positionAttr = addAttribute(PositionIncrementAttribute.class);



    private Set<String> filter;


    /**
     * 单文档当前所在的总offset，当reset（切换multi-value fields中的value）的时候不清零，在end（切换field）时清零
     */
    private int totalOffset = 0;

    private HanLp2Wrapper hanLp2Wrapper;

    public HanLP2Tokenizer(HanLp2 hanLp2, Set<String> filter)
    {
        super();
        hanLp2Wrapper = new HanLp2Wrapper(input,hanLp2);
        this.filter = filter;
    }


    @Override
    final public boolean incrementToken() throws IOException
    {
        clearAttributes();
        int position = 0;
        HanLp2.Term term;
        boolean un_increased = true;
        do
        {
            term = hanLp2Wrapper.next();
            if (term == null)
            {
                break;
            }
            if (TextUtility.isBlank(term.word)) // 过滤掉空白符，提高索引效率
            {
                continue;
            }
            if (filter!=null && filter.contains(term.word)){
                continue;
            }
            else
            {
                ++position;
                un_increased = false;
            }
        }
        while (un_increased);

        if (term != null)
        {
            positionAttr.setPositionIncrement(position);
            termAtt.setEmpty().append(term.word);
            offsetAtt.setOffset(correctOffset(totalOffset + term.start),
                    correctOffset(totalOffset + term.end));
            return true;
        }
        else
        {
            totalOffset += hanLp2Wrapper.offset;
            return false;
        }
    }

    @Override
    public void end() throws IOException
    {
        super.end();
        offsetAtt.setOffset(totalOffset, totalOffset);
        totalOffset = 0;
    }

    /**
     * 必须重载的方法，否则在批量索引文件时将会导致文件索引失败
     */
    @Override
    public void reset() throws IOException
    {
        super.reset();
        hanLp2Wrapper.reset(new BufferedReader(this.input));
    }

}
