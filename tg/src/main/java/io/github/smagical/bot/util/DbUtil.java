package io.github.smagical.bot.util;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DbUtil {
    private static String jdbcURL = "jdbc:postgresql://localhost:5432/tg_bot";
    private static String jdbcUsername = "postgres";
    private static String jdbcPassword = "980920";
    private static Connection conn;

    static {
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {

        }

        String path = DbUtil.class.getClassLoader()
                .getResource(".")
                .getPath();
        File[] sql = new File(path).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".sql");
            }
        });

        for (File file : sql) {
            try (
                    FileReader reader = new FileReader(file);
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
