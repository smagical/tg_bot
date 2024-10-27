package io.github.smagical.bot.util;

import io.github.smagical.bot.TgInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class DbUtil {
    private static String jdbcURL = TgInfo.getProperty(TgInfo.JDBC_URL);
    private static String jdbcUsername = TgInfo.getProperty(TgInfo.JDBC_USERNAME);
    private static String jdbcPassword = TgInfo.getProperty(TgInfo.JDBC_PASSWORD);
    private static Connection conn;
    private final static List<String> fileList =
            Arrays.asList("ip.sql", "tg_bot_group.sql", "tg_messages.sql");

    static {
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {

        }
        for (String sql : fileList) {
            try (
                    InputStreamReader reader = new InputStreamReader(
                            DbUtil.class.getClassLoader().getResourceAsStream(sql)
                    );
                    BufferedReader br = new BufferedReader(reader)
            ){
                StringBuilder builder = new StringBuilder();
                br.lines().forEach(builder::append);
                Statement statement = getConnection().createStatement();
                boolean run  = statement.execute(builder.toString());
                System.out.println(run + " "+builder.toString());
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }



    };
    public static Connection getConnection() {
        return conn;
    }

}
