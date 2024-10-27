package io.github.smagical.bot.plugin;

import io.github.smagical.bot.util.DbUtil;

import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public interface BotDb {
    public final static String END = new String(new byte[]{(byte) 0xe2,(byte) 0x80, (byte) 0x8b}, StandardCharsets.UTF_8);
     static final String SELECT_BY_USER =
            "SELECT * FROM tg_bot_group WHERE user_id = ?";
     static final String DEL_GROUP_BY_USER =
            "DELETE  FROM tg_bot_group WHERE chat_id = ? AND user_id = ?";
     static final String INSERT =
            "INSERT  INTO tg_bot_group VALUES(?,?)";
    static final String SELECT_BY_CHAT =
            "SELECT * FROM tg_bot_group WHERE chat_id = ?";
    public static List<Long> getChatFrom(Long userId) {
        List<Long> list = new ArrayList<>();
        try {
            PreparedStatement statement =  DbUtil.getConnection().prepareStatement(SELECT_BY_USER);
            statement.setLong(1,userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()){
                list.add(resultSet.getLong("chat_id"));
            }
        } catch (SQLException e) {
        }
        return list;
    }
    public  static boolean delChatFrom(Long chatId,Long userId) {
        try {
            PreparedStatement statement =  DbUtil.getConnection().prepareStatement(DEL_GROUP_BY_USER);
            statement.setLong(1,chatId);
            statement.setLong(2,userId);
            return statement.executeUpdate()>0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public  static boolean addChatFrom(Long chatId,Long userId) {

        try {
            PreparedStatement statement =  DbUtil.getConnection().prepareStatement(INSERT);
            statement.setLong(1,chatId);
            statement.setLong(2,userId);
            return statement.executeUpdate()>0;
        } catch (SQLException e) {
            return false;
        }
    }
    public static boolean chatExist(Long chatId) {
        try {
            PreparedStatement statement =  DbUtil.getConnection().prepareStatement(SELECT_BY_CHAT);
            statement.setLong(1,chatId);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
