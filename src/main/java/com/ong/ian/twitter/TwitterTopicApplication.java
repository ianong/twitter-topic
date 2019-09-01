package com.ong.ian.twitter;

import com.ong.ian.twitter.config.AppConfig;
import com.ong.ian.twitter.consumer.StatusTopicConsumer;
import com.ong.ian.twitter.producer.FeedStreamProducer;
import twitter4j.Status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TwitterTopicApplication {

    public static void main(String [] args) {

        Properties properties = AppConfig.getInstance().getProperties();
        Scanner scanner = new Scanner(System.in);

        int numOfTopics = Integer.parseInt(properties.getProperty(AppConfig.KEY_NUM_OF_TOPICS, "5"));
        System.out.println(String.format("Please enter %d topics (e.g.: twitter, basketball, trump, etc.)", numOfTopics));

        // Enter topics
        int topicsIdx = 0;
        List<String> topics = new ArrayList<>();
        while(topicsIdx < numOfTopics) {
            System.out.print(String.format("Topic %d: ", topicsIdx + 1));
            String topicInput = scanner.nextLine();
            if (!topics.contains(topicInput)) {
                topics.add(topicInput.toLowerCase());
                topicsIdx++;
            } else {
                System.err.println(String.format("Topic '%s' already exists!", topicInput));
            }
        }

        // Create threads (1 producer - x consumers, where x is the topic count)
        int queueSize = Integer.parseInt(properties.getProperty(AppConfig.KEY_QUEUE_SIZE, "5"));
        Map<String, BlockingQueue<Status>> queueByTopic = topics.stream()
            .collect(Collectors.toMap(t -> t, t -> new LinkedBlockingQueue<>(queueSize)));

        ExecutorService exService = Executors.newFixedThreadPool(topics.size() + 1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdownAndAwaitTermination(exService);
        }));

        Collection<Future<?>> futures = new ArrayList<>();

        FeedStreamProducer producer = new FeedStreamProducer(queueByTopic);
        futures.add(exService.submit(producer));

        queueByTopic.keySet().stream()
            .map(topic -> new StatusTopicConsumer(topic, queueByTopic.get(topic)))
            .forEach(consumer -> futures.add(exService.submit(consumer)));

        // Start threads
        futures.forEach(future -> {
            try {
                future.get();
            } catch (ExecutionException | InterruptedException e) {
                System.err.println("Error occurred when running threads!");
            }
        });
    }

    private static void shutdownAndAwaitTermination(ExecutorService pool) {
        System.out.println("Shutting Down Pool...");
        pool.shutdown();
        try {
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
        }
    }
}
