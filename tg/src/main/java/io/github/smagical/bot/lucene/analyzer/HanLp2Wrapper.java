package io.github.smagical.bot.lucene.analyzer;

import io.github.smagical.bot.hanlp.HanLp2;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

class HanLp2Wrapper implements AutoCloseable{

    private Reader input;
    private HanLp2 hanLp2;

    /**
     * 分词结果
     */
    private Iterator<HanLp2.Term> iterator;
    /**
     * term的偏移量，由于wrapper是按行读取的，必须对term.offset做一个校正
     */
    int offset;
    /**
     * 缓冲区大小
     */
    private static final int BUFFER_SIZE = 1024 * 1024;
    /**
     * 缓冲区
     */
    private char[] buffer = new char[BUFFER_SIZE];
    /**
     * 缓冲区未处理的下标
     */
    private int remainSize = 0;


    /**
     * 句子分隔符
     */
    private static final Set<Character> delimiterCharSet = new HashSet<Character>()
    {{
        add('\r');
        add('\n');
        add(';');
        add('；');
        add('。');
        add('!');
        add('！');
        add('.');
        add(' ');
        add(',');
        add('\t');
        add('，');
    }};

    private int lastOffset = 0;
    private int lastWordsOffset = 0;
    private int lastSentenceLength = 0;


    public HanLp2Wrapper(Reader reader, HanLp2 hanLp2) {
        this.input = reader;
        this.hanLp2 = hanLp2;
    }
    /**
     * 重置分词器
     *
     * @param reader
     */
    public void reset(Reader reader)
    {
        input = reader;
        offset = 0;
        lastOffset = 0;
        lastSentenceLength = 0;
        lastWordsOffset = -1;
        iterator = null;
    }

    public HanLp2.Term next() throws IOException
    {
        if (iterator != null && iterator.hasNext()) {
            HanLp2.Term term =  iterator.next();
            if (term.start == 0){
                lastOffset += lastWordsOffset + 1;
            }
            lastWordsOffset = term.end;
            term.start += lastOffset + offset;
            term.end += lastOffset + offset;
            return term;
        }

        String line = readLine();
        if (line == null) return null;
        List<HanLp2.Term> termList = hanLp2.seg(line);
        if (termList.size() == 0) return null;
//        for (HanLp2.Term term : termList)
//        {
//            term.start += offset;
//            term.end += offset;
//        }

        offset += lastSentenceLength;
        lastSentenceLength = line.length();
        iterator = termList.iterator();
        return next();
    }

    private String readLine() throws IOException
    {
        int offset = 0;
        int length = BUFFER_SIZE;
        if (remainSize > 0)
        {
            offset = remainSize;
            length -= remainSize;
        }
        int n = input.read(buffer, offset, length);
        if (n < 0)
        {
            if (remainSize != 0)
            {
                String lastLine = new String(buffer, 0, remainSize);
                remainSize = 0;
                return lastLine;
            }
            return null;
        }
        n += offset;

        int eos = lastIndexOfEos(buffer, n);
        String line = new String(buffer, 0, eos);
        remainSize = n - eos;
        System.arraycopy(buffer, eos, buffer, 0, remainSize);
        return line;
    }

    private int lastIndexOfEos(char[] buffer, int length)
    {
        for (int i = length - 1; i > 0; i--)
        {
            if (delimiterCharSet.contains(buffer[i]))
            {
                return i + 1;
            }
        }
        return length;
    }


    @Override
    public void close()  {
        try {
            hanLp2.close();
        } catch (Exception e) {

        }
    }
}
