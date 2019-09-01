package com.ong.ian.twitter.producer;

import com.ong.ian.twitter.config.AppConfig;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

public class FeedStreamProducer implements Runnable {

    private Map<String, BlockingQueue<Status>> queuesByTopic;

    public FeedStreamProducer(Map<String, BlockingQueue<Status>> queuesByTopic) {
        this.queuesByTopic = queuesByTopic;
    }

    @Override
    public void run() {

        StatusListener listener = new StatusListener() {

            @Override
            public void onException(Exception e) {
                // do nothing
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice arg) {
                // do nothing
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                // do nothing
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                // do nothing
            }

            @Override
            public void onStatus(Status status) {
                String statusText = status.getText().toLowerCase();

                queuesByTopic.keySet().parallelStream()
                    .filter(statusText::contains)
                    .forEach(matchedTopic -> {
                        try {
                            BlockingQueue<Status> queueByTopic = queuesByTopic.get(matchedTopic);
                            // Thread will wait if there is no space available.
                            queueByTopic.put(status);
                        } catch (InterruptedException e) {
                            System.err.println(String.format("Error occurred when adding status to '%s' queue!", matchedTopic));
                        }
                    });
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                // do nothing
            }
        };

        Properties properties = AppConfig.getInstance().getProperties();

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(Boolean.parseBoolean(properties.getProperty(AppConfig.KEY_TWITTER_DEBUG_ENABLED, "true")))
            .setOAuthConsumerKey(properties.getProperty(AppConfig.KEY_TWITTER_CONSUMER_KEY))
            .setOAuthConsumerSecret(properties.getProperty(AppConfig.KEY_TWITTER_CONSUMER_SECRET))
            .setOAuthAccessToken(properties.getProperty(AppConfig.KEY_TWITTER_ACCESS_TOKEN))
            .setOAuthAccessTokenSecret(properties.getProperty(AppConfig.KEY_TWITTER_ACCESS_TOKEN_SECRET));

        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();

        twitterStream.addListener(listener);

        String[] trackArray = queuesByTopic.keySet().toArray(new String[0]);
        FilterQuery filter = new FilterQuery(trackArray).language("en");
        twitterStream.filter(filter);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting Down Producer...");
            twitterStream.shutdown();
        }));
    }
}
