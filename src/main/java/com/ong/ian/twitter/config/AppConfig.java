package com.ong.ian.twitter.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    // Application configuration
    public static final String KEY_NUM_OF_TOPICS = "app.numberOfTopics";
    public static final String KEY_QUEUE_SIZE = "app.queueSize";
    public static final String KEY_DIR_PATH = "app.directoryPath";

    // Twitter configuration
    public static final String KEY_TWITTER_DEBUG_ENABLED = "twitter.debug";
    public static final String KEY_TWITTER_CONSUMER_KEY = "twitter.oauth.consumerKey";
    public static final String KEY_TWITTER_CONSUMER_SECRET = "twitter.oauth.consumerSecret";
    public static final String KEY_TWITTER_ACCESS_TOKEN = "twitter.oauth.accessToken";
    public static final String KEY_TWITTER_ACCESS_TOKEN_SECRET = "twitter.oauth.accessTokenSecret";

    private static AppConfig INSTANCE;
    private static Properties PROPERTIES;

    private AppConfig() {
    }

    public static AppConfig getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new AppConfig();
        }

        return INSTANCE;
    }

    public Properties getProperties() {
        if (PROPERTIES == null) {

            String propertiesFileName = "application.properties";

            InputStream inputStream;

            try {
                System.out.println("Reading from overridden " + propertiesFileName);
                inputStream = new FileInputStream("./" + propertiesFileName);
            } catch (FileNotFoundException e) {
                System.out.println("Reading from default " + propertiesFileName);
                inputStream = AppConfig.class.getClassLoader().getResourceAsStream(propertiesFileName);
            }

            try {
                PROPERTIES = new Properties();
                PROPERTIES.load(inputStream);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Error occurred when reading properties file: '%s'", propertiesFileName));
            }
        }

        return PROPERTIES;
    }
}
