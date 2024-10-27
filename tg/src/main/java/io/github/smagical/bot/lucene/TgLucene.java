package io.github.smagical.bot.lucene;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hankcs.hanlp.HanLP;
import io.github.smagical.bot.lucene.analyzer.HanLP2Analyzer;
import io.github.smagical.bot.lucene.analyzer.HanLPAnalyzer;
import io.github.smagical.bot.util.DbUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.*;
import org.apache.lucene.sandbox.queries.FuzzyLikeThisQuery;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


@Slf4j
public class TgLucene implements AutoCloseable{

    private final static String SELECT =
            "SELECT * FROM tg_messages";

    private String indexDir;
    private FSDirectory indexFsDirectory;
    private FSDirectory facesFsDirectory;
    private FacetsConfig facetsConfig;
    private IndexWriterConfig indexWriterConfig;
    private ObjectMapper objectMapper;
    private IndexWriter indexWriter;
    private TaxonomyWriter taxonomyWriter;
    private Analyzer analyzer;


    @Override
    public void close() throws Exception {
        IOUtils.close(facesFsDirectory,indexFsDirectory);
        if (analyzer instanceof HanLP2Analyzer) ((HanLP2Analyzer)analyzer).close();
    }

    public static class TgMessage implements Serializable {
        long id;
        long chatId;
        long album;
        String content;
        String link;
        Map<String,Object> other;

        public long getId() {
            return id;
        }

        public long getChatId() {
            return chatId;
        }

        public long getAlbum() {
            return album;
        }

        public String getContent() {
            return content;
        }

        public String getLink() {
            return link;
        }

        public Map<String, Object> getOther() {
            return other;
        }

        @Override
        public String toString() {
            try {
                return JsonMapper.builder()
                        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).build().writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public  static class PageHelper implements Serializable{
        private int pageSize;

        public PageHelper(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getPageSize() {
            return pageSize;
        }
    }

    public static class ScoreDocPageHelper extends PageHelper{
        private ScoreDoc lastScoreDoc;


        public ScoreDocPageHelper(int pageSize, ScoreDoc lastScoreDoc) {
            super(pageSize);
            this.lastScoreDoc = lastScoreDoc;

        }

        public ScoreDoc getLastScoreDoc() {
            return lastScoreDoc;
        }


    }

    public static class TgPageHelper extends PageHelper{
        private int page;

        public TgPageHelper(int pageSize, int page) {
            super(pageSize);
            this.page = page;
        }

        public int getPage() {
            return page;
        }
    }

    public final static class TgMessagePage implements Serializable {
        private int total;
        private int pageSize;
        private int page;
        private List<TgMessage> messages;
        private ScoreDoc lastScoreDoc;

        public TgMessagePage(int total, int pageSize, int page, List<TgMessage> messages, ScoreDoc lastScoreDoc) {
            this.total = total;
            this.pageSize = pageSize;
            this.page = page;
            this.messages = messages;
            this.lastScoreDoc = lastScoreDoc;
        }

        public int getTotal() {
            return total;
        }

        public int getPageSize() {
            return pageSize;
        }

        public List<TgMessage> getMessages() {
            return messages;
        }

        public ScoreDoc getLastScoreDoc() {
            return lastScoreDoc;
        }

        public int getPage() {
            return page;
        }
    }


    public TgLucene(String dir) throws IOException {
        this(dir,new HanLPAnalyzer(
                HanLP.newSegment("crf").enableJapaneseNameRecognize(true)
                        .enableMultithreading(true)
                        .enablePlaceRecognize(true)
        ));
    }

    public TgLucene(String dir,Analyzer analyzer) {
        this.indexDir = dir;
        this.analyzer = analyzer;
        try {
            File indexDirFile = new File(indexDir+"/index");
            if (!indexDirFile.exists()) {
                indexDirFile.mkdirs();
            }
            indexFsDirectory = FSDirectory.open(indexDirFile.toPath());
            indexWriterConfig = new IndexWriterConfig(
                    analyzer
            );
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            indexWriterConfig.setCommitOnClose(true);

            File facesDir = new File(indexDir+"/faces");
            if (!facesDir.exists()) {
                facesDir.mkdirs();
            }
            facesFsDirectory = FSDirectory.open(facesDir.toPath());
            facetsConfig = new FacetsConfig();

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        close();
                    } catch (Exception e) {

                    }
                }
            });
            objectMapper =
                    JsonMapper.builder()
                            .addModule(new JavaTimeModule())
                            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                            .disable(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS)
                            .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void buildIndex(int count) throws SQLException, IOException {
        indexWriter = new IndexWriter(indexFsDirectory,indexWriterConfig);
        taxonomyWriter = new DirectoryTaxonomyWriter(facesFsDirectory,
                IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        if (count == -1)
            count = Integer.MAX_VALUE;
        PreparedStatement stmt =
                DbUtil.getConnection().prepareStatement(SELECT);
        stmt.setFetchSize(100);
        ResultSet rs = stmt.executeQuery();
        while (rs.next() && count > 0) {
            TgMessage msg = new TgMessage();
            msg.id = rs.getLong(1);
            msg.chatId = rs.getLong(2);
            msg.album = rs.getLong(3);
            msg.content = rs.getString(4);
            msg.link = rs.getString(5);
            msg.other = new ObjectMapper().readValue(rs.getString(6),Map.class);
            if (AD.adFilter(msg)) continue;
            if (exit(msg.chatId,msg.id)) continue;
            insertTgMessage(msg);
            count--;
            if (count % 200 == 0)
                merger();
        }
        merger();
        IOUtils.close(taxonomyWriter,indexWriter);
    }

    public void insertTgMessage(TgMessage msg) throws SQLException, IOException {
        Document document = new Document();
        document.add(
                new LongField("id",msg.id, Field.Store.YES)
        );
        document.add(
                new LongField("chatId",msg.chatId, Field.Store.YES)
        );
        document.add(
                new LongField("album",msg.album, Field.Store.YES)
        );
        document.add(
                new TextField("content",msg.content, Field.Store.YES)
        );
        document.add(
                new TextField("content_simple",HanLP.convertToSimplifiedChinese(msg.content).toUpperCase(Locale.ROOT), Field.Store.YES)
        );
        document.add(
                new StringField("link",msg.link, Field.Store.YES)
        );
        document.add(
                new StoredField("other",objectMapper.writeValueAsString(msg.other))
        );

        indexWriter.addDocument(facetsConfig.build(taxonomyWriter,document));

    }
    public List<TgMessage> query(String text) throws IOException {
        return query(text,null,10);
    }
    public List<TgMessage> query(String text,ScoreDoc scoreDoc,int limit) throws IOException {
        IndexReader reader = DirectoryReader.open(indexFsDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);
        TermQuery termQuery = new TermQuery(new Term("content_simple",text));
        TopDocs docs = searcher.searchAfter(scoreDoc,termQuery,limit);

        List<TgMessage> results = new ArrayList<TgMessage>();
        for (ScoreDoc doc : docs.scoreDocs) {
            TgMessage msg = new TgMessage();
            Document document = reader.storedFields().document(doc.doc);
            msg.id = document.getField("id").storedValue().getLongValue();
            msg.chatId = document.getField("chatId").storedValue().getLongValue();
            msg.album = document.getField("album").storedValue().getLongValue();
            msg.content = document.getField("content").storedValue().getStringValue();
            msg.link = document.getField("link").storedValue().getStringValue();
            msg.other = objectMapper.readValue(document.getField("other").storedValue().getStringValue(), Map.class);
            results.add(msg);
        }
        reader.close();
        return results;
    }

    public List<TgMessage> queryMoreLikeThis(String text) throws IOException {
        return queryMoreLikeThis(text,null,10);
    }

    public List<TgMessage> queryMoreLikeThis(String text,ScoreDoc scoreDoc,int limit) throws IOException {
        IndexReader reader = DirectoryReader.open(indexFsDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);
        FuzzyLikeThisQuery fuzzyLikeThisQuery = new FuzzyLikeThisQuery(100,this.analyzer);
        fuzzyLikeThisQuery.addTerms(text,"content_simple",0f,0);
        TopDocs docs = searcher.searchAfter(scoreDoc,fuzzyLikeThisQuery.rewrite(searcher),limit);
        System.out.println(searcher.count(fuzzyLikeThisQuery.rewrite(searcher)));
        List<TgMessage> results = new ArrayList<TgMessage>();

        for (ScoreDoc doc : docs.scoreDocs) {
            TgMessage msg = new TgMessage();
            Document document = reader.storedFields().document(doc.doc);
            msg.id = document.getField("id").storedValue().getLongValue();
            msg.chatId = document.getField("chatId").storedValue().getLongValue();
            msg.album = document.getField("album").storedValue().getLongValue();
            msg.content = document.getField("content").storedValue().getStringValue();
            msg.link = document.getField("link").storedValue().getStringValue();
            msg.other = objectMapper.readValue(document.getField("other").storedValue().getStringValue(), Map.class);
            results.add(msg);
        }
        reader.close();
        return results;
    }

    public TgMessagePage query(String text,TgPageHelper pageHelper) throws IOException {
        IndexReader reader = DirectoryReader.open(indexFsDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);
        BooleanQuery query = new BooleanQuery.Builder()
                .build();
        TermQuery termQuery = new TermQuery(new Term("content_simple",text));
        int total = searcher.count(termQuery.rewrite(searcher));
        List<TgMessage> results = new ArrayList<TgMessage>();
        if (total <= pageHelper.page * pageHelper.getPageSize()){
            return new TgMessagePage(total,0, pageHelper.getPage(), results,null);
        }
        TopDocs docs = searcher.search(termQuery,pageHelper.getPageSize() + pageHelper.page * pageHelper.getPageSize());
        int start = pageHelper.page * pageHelper.getPageSize();
        ScoreDoc lastDoc = docs.scoreDocs[docs.scoreDocs.length - 1];
        for (int i = start + 1; i < Math.min(docs.scoreDocs.length, start + pageHelper.getPageSize()); i++) {
            TgMessage msg = new TgMessage();
            Document document = reader.storedFields().document(docs.scoreDocs[i].doc);
            msg.id = document.getField("id").storedValue().getLongValue();
            msg.chatId = document.getField("chatId").storedValue().getLongValue();
            msg.album = document.getField("album").storedValue().getLongValue();
            msg.content = document.getField("content").storedValue().getStringValue();
            msg.link = document.getField("link").storedValue().getStringValue();
            msg.other = objectMapper.readValue(document.getField("other").storedValue().getStringValue(), Map.class);
            results.add(msg);
        }

        reader.close();
        return new TgMessagePage(total,results.size(), pageHelper.getPage(), results,lastDoc);
    }
    public TgMessagePage query(String text,ScoreDocPageHelper pageHelper) throws IOException {
        IndexReader reader = DirectoryReader.open(indexFsDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);
        BooleanQuery query = new BooleanQuery.Builder()
                .build();
        TermQuery termQuery = new TermQuery(new Term("content_simple",text));
        int total = searcher.count(termQuery.rewrite(searcher));
        List<TgMessage> results = new ArrayList<TgMessage>();

        TopDocs docs = searcher.searchAfter(pageHelper.getLastScoreDoc(),termQuery, pageHelper.getPageSize());
        ScoreDoc lastDoc = docs.scoreDocs[docs.scoreDocs.length - 1];
        for (ScoreDoc scoreDoc : docs.scoreDocs) {
            TgMessage msg = new TgMessage();
            Document document = reader.storedFields().document(scoreDoc.doc);
            msg.id = document.getField("id").storedValue().getLongValue();
            msg.chatId = document.getField("chatId").storedValue().getLongValue();
            msg.album = document.getField("album").storedValue().getLongValue();
            msg.content = document.getField("content").storedValue().getStringValue();
            msg.link = document.getField("link").storedValue().getStringValue();
            msg.other = objectMapper.readValue(document.getField("other").storedValue().getStringValue(), Map.class);
            results.add(msg);
        }

        reader.close();
        return new TgMessagePage(total,results.size(), -1, results,lastDoc);
    }

    public TgMessagePage queryMoreLikeThis(String text,TgPageHelper pageHelper) throws IOException {
        IndexReader reader = DirectoryReader.open(indexFsDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);
        FuzzyLikeThisQuery fuzzyLikeThisQuery = new FuzzyLikeThisQuery(100,this.analyzer);
        fuzzyLikeThisQuery.addTerms(text,"content_simple",0f,0);
        int total = searcher.count(fuzzyLikeThisQuery.rewrite(searcher));
        List<TgMessage> results = new ArrayList<TgMessage>();
        if (total <= pageHelper.page * pageHelper.getPageSize()){
            return new TgMessagePage(total,0, pageHelper.getPage(), results,null);
        }
        TopDocs docs = searcher.search(fuzzyLikeThisQuery.rewrite(searcher),pageHelper.getPageSize() + pageHelper.page * pageHelper.getPageSize());
        int start = pageHelper.page * pageHelper.getPageSize();
        ScoreDoc lastDoc = docs.scoreDocs[docs.scoreDocs.length - 1];
        for (int i = start + 1; i < Math.min(docs.scoreDocs.length, start + pageHelper.getPageSize()); i++) {
            TgMessage msg = new TgMessage();
            Document document = reader.storedFields().document(docs.scoreDocs[i].doc);
            msg.id = document.getField("id").storedValue().getLongValue();
            msg.chatId = document.getField("chatId").storedValue().getLongValue();
            msg.album = document.getField("album").storedValue().getLongValue();
            msg.content = document.getField("content").storedValue().getStringValue();
            msg.link = document.getField("link").storedValue().getStringValue();
            msg.other = objectMapper.readValue(document.getField("other").storedValue().getStringValue(), Map.class);
            results.add(msg);
        }

        reader.close();
        return new TgMessagePage(total,results.size(), pageHelper.getPage(), results,lastDoc);
    }
    public TgMessagePage queryMoreLikeThis(String text,ScoreDocPageHelper pageHelper) throws IOException {
        IndexReader reader = DirectoryReader.open(indexFsDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);
        FuzzyLikeThisQuery fuzzyLikeThisQuery = new FuzzyLikeThisQuery(100,this.analyzer);
        fuzzyLikeThisQuery.addTerms(text,"content_simple",0f,0);
        int total = searcher.count(fuzzyLikeThisQuery.rewrite(searcher));
        List<TgMessage> results = new ArrayList<TgMessage>();
        TopDocs docs = searcher.searchAfter(pageHelper.getLastScoreDoc(),fuzzyLikeThisQuery.rewrite(searcher), pageHelper.getPageSize());
        ScoreDoc lastDoc = docs.scoreDocs[docs.scoreDocs.length - 1];
        for (ScoreDoc scoreDoc : docs.scoreDocs) {
            TgMessage msg = new TgMessage();
            Document document = reader.storedFields().document(scoreDoc.doc);
            msg.id = document.getField("id").storedValue().getLongValue();
            msg.chatId = document.getField("chatId").storedValue().getLongValue();
            msg.album = document.getField("album").storedValue().getLongValue();
            msg.content = document.getField("content").storedValue().getStringValue();
            msg.link = document.getField("link").storedValue().getStringValue();
            msg.other = objectMapper.readValue(document.getField("other").storedValue().getStringValue(), Map.class);
            results.add(msg);
        }

        reader.close();
        return new TgMessagePage(total,results.size(), -1, results,lastDoc);
    }


    public boolean exit(Long chatId,Long id) throws IOException {
        IndexReader reader = null;
        if (indexWriter!=null && indexWriter.isOpen())  reader = DirectoryReader.open(indexWriter);
        else reader = DirectoryReader.open(indexFsDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);
        BooleanQuery query = new BooleanQuery.Builder()
                .add( LongField.newExactQuery("chatId",chatId), BooleanClause.Occur.MUST)
                .add( LongField.newExactQuery("id",id), BooleanClause.Occur.MUST)
                .build();

        TopDocs docs = searcher.search(query,1);
        if (reader!=null)
            reader.close();
        return docs.totalHits.value() > 0;
    }

    private boolean merger() throws IOException {
        indexWriter.forceMerge(1);
        indexWriter.forceMergeDeletes();
        return true;
    }


}
