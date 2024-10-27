package io.github.smagical.bot;

import io.github.smagical.bot.bot.HandlerWrapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class TgInfo  {
     private final static Properties prop = new Properties();
     public final static boolean DEBUG = false;
     static {


         try {
             prop.load(HandlerWrapper.class.getClassLoader().getResourceAsStream("tg.properties"));
             String path = System.getProperty("tginfo", "");
             if (!path.equals("")){
                 prop.load(new FileInputStream(path));
             }

         } catch (IOException e) {

         }
     }
     public final static String API_ID = "api_id";
     public final static String API_HASH = "api_hash";
     public final static String TITLE = "title";
     public final static String SHORT_NAME = "short_name";
     public final static String APPLICATION_VERSION = "application_version";
     public final static String LANGUAGE_CODE = "language_code";
     public final static String USE_TEST = "use_test";
     public final static String DATABASE_ENCRYPTION_KEY = "database_encryption_key";
     public final static String REQUEST_RETRY = "request_retry";
    public final static String LUCENE_DIR = "lucene_dir";
    public final static String LUCENE_ANALYZER = "lucene_analyzer";
    public final static String PYTHONHOME = "python_home";
    public final static String JDBC_URL = "jdbc_url";
    public final static String JDBC_USERNAME = "jdbc_username";
    public final static String JDBC_PASSWORD = "jdbc_password";

    public final static String LOGIN_TYPE = "login_type";
    public final static String LOGIN_TOKEN = "login_token";
    public final static String LOGIN_NUMBER = "login_number";

    public static String getProperty(String key) {
         return prop.getProperty(key);
     }
    public static int getPropertyInt(String key) {
        return Integer.parseInt(prop.getProperty(key));
    }
    public static boolean getPropertyBool(String key) {
        return Boolean.parseBoolean(prop.getProperty(key));
    }
}
