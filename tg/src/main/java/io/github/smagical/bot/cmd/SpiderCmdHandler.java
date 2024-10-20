package io.github.smagical.bot.cmd;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.bot.util.ClientUtils;
import io.github.smagical.bot.util.DbUtil;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static io.github.smagical.bot.cmd.MainCmd.printSplit;
import static io.github.smagical.bot.cmd.MainCmd.select;

@Slf4j
public class SpiderCmdHandler implements Cmd{
    private final static int CHUNCK = 200;
    private final static SpiderCmdHandler cmdHandler = new SpiderCmdHandler();

    final public static SpiderCmdHandler getInstance() {
        return cmdHandler;
    }

    public static void printSpiderMenu(Bot bot) {
        while (true){
            printSplit();
            log.info("###################  Spider Menu  #########################");
            log.info("select\tid\ttitle");
            List<TdApi.Chat> chats = bot.getAllChats().getAllChat().stream().toList();
            for (int i = 0; i < chats.size(); i++) {
                log.info("{}\t{}\t{}",i,chats.get(i).id,chats.get(i).title);
            }
            log.info("{}\t{}\t{}",chats.size(),"update ALL","update ALL");
            log.info("{}\t{}\t{}",chats.size()+1,"exit","go back");
            printSplit();
            int select = -1;
            try {
                select = Integer.parseInt(select("请选择:\n"));
                if (select == chats.size()){
                    update(bot);
                } else if (select == chats.size()+1){
                    break;
                }else if (select >=0  && select < chats.size()){
                    printCl(bot,chats.get(select).id);
                }
            }catch (Exception e){}

        }
    }
    public static void printCl(Bot bot,Long chatId) {
        while (true){
            printSplit();
            log.info("###################  Pull Menu  #########################");
            log.info("###################  {}  #########################",chatId);
            log.info("###################  0\tspider #########################");
            log.info("###################  1\tupdate #########################");
            log.info("###################  2\tgo back spider menu #########################");
            printSplit();


            while (true){
                String select = select("选择");
                if ("0".equals(select)){
                    long messageID = -1;
                    int limit = -1;
                    try {
                        messageID = Long.parseLong(select("请输入最后的messageID:\n"));
                    }catch (Exception e){}
                    try {
                        limit = Integer.parseInt(select("请输入limit -1表示直到数据库中存在停止:\n"));
                    }catch (Exception e){}
                    spider(bot,chatId,messageID,limit);
                }else if ("1".equals(select)){
                    spider(bot,chatId,0,-1);
                }else if ("2".equals(select)){
                    return;
                }
            }

        }
    }

    public static void spider(Bot bot,Long chatId,long messageID,int limit) {
        boolean all = false;
        if (limit == -1) {
            all = true;
            limit = Integer.MAX_VALUE/2;
        }
        int count = (limit + CHUNCK -1)/CHUNCK;
        for (int i = 0; i < count; i++) {
            try {
                Collection<TdApi.Message>  messages =
                        ClientUtils.getChatHistory(bot.getClient(),chatId,messageID,limit > CHUNCK?CHUNCK:limit);
                if (messages.isEmpty()) break;
                for (TdApi.Message message : messages) {
                    TgMessage tgMessage = new TgMessage();
                    tgMessage.album = message.mediaAlbumId;
                    tgMessage.id = message.id;
                    tgMessage.chatId = chatId;
                    tgMessage.other = new HashMap<>();
                    switch (message.content.getConstructor()){
                        case TdApi.MessageText.CONSTRUCTOR -> {
                            solveText((TdApi.MessageText)message.content,tgMessage);
                            break;
                        }case TdApi.MessagePhoto.CONSTRUCTOR -> {
                            solvePhoto((TdApi.MessagePhoto)message.content,tgMessage);
                            break;
                        }case TdApi.MessageVideo.CONSTRUCTOR -> {
                            solveVideo((TdApi.MessageVideo)message.content,tgMessage);
                            break;
                        }
                    }
                    if (exits(message.id,chatId) ) {
                        if (all)
                         break;
                    } else  save(bot,tgMessage);

                }
                limit -= CHUNCK;
                messageID = messages.iterator().next().id;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        log.info("last messages id {}",messageID);
    }

    public static void update(Bot bot){
        try {
            PreparedStatement ps = DbUtil.getConnection().prepareStatement(SELECT_CHAT_ID);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()){
                Long id = resultSet.getLong(1);
                spider(bot,id,0,-1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private static void solveText(TdApi.MessageText text,TgMessage tgMessage){
        tgMessage.content = text.text.text;
        tgMessage.other.put("type","text");
    }
    private static void solvePhoto(TdApi.MessagePhoto photo,TgMessage tgMessage){
        tgMessage.content = photo.caption.text;
        List<Object> files = new ArrayList<>();
        for (TdApi.PhotoSize size : photo.photo.sizes) {
            files.add(
                    Map.of("type",size.type,
                            "id",size.photo.remote.id,
                            "size",size.photo.size
                            )
            );
        }
        tgMessage.other.put("file",files);
        tgMessage.other.put("type","photo");
    }
    private static void solveVideo(TdApi.MessageVideo video,TgMessage tgMessage){
        tgMessage.content = video.caption.text;
        List<Object> files = new ArrayList<>();
        files.add(
                Map.of("type","video",
                        "id",video.video.video.remote.id,
                        "size",video.video.video.size,
                        "fileName",video.video.fileName,
                        "mimeType",video.video.mimeType
                        )
        );
        tgMessage.other.put("file",files);
        tgMessage.other.put("type","video");
    }

    private static void save(Bot bot,TgMessage message){
        try {
            if (message.content != null) {
                message.link = ClientUtils.getMessageLink(
                        bot.getClient(), message.chatId, message.id
                );
            }
            PreparedStatement statement = DbUtil.getConnection()
                    .prepareStatement(INSERT);
            statement.setLong(1,message.id);
            statement.setLong(2,message.chatId);
            statement.setLong(3,message.album);
            statement.setString(4,message.content);
            statement.setString(5,message.link);
            statement.setString(6,new ObjectMapper().writeValueAsString(message.other));
            boolean exec = statement.execute();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static boolean exits(Long id,Long chatId) {
        try {
            PreparedStatement statement = DbUtil.getConnection().prepareStatement(SELECT_BY_ID);
            statement.setLong(1,id);
            statement.setLong(2,chatId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return true;
            }else {
                return false;
            }
        }catch (Exception e){
            return false;
        }

    }

    @Override
    public void printMainMenu(Bot bot) {
        printSpiderMenu(bot);
    }

    private static final String INSERT =
            "INSERT INTO tg_messages values(?,?,?,?,?,?)";
    private static final String SELECT_BY_ID =
            "SELECT * FROM tg_messages WHERE id = ? AND chat_id = ?";
    private static final String UPDATE =
            "UPDATE tg_messages set other = ? where id = ?";
    private static final String SELECT_CHAT_ID =
            "SELECT DISTINCT tg_messages.chat_id FROM tg_messages";

    private static class TgMessage{
        long id;
        long album;
        long chatId;
        String content;
        String link;
        Map<String,Object> other;
    }

}
