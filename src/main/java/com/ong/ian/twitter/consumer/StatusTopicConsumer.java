package com.ong.ian.twitter.consumer;

import com.ong.ian.twitter.writer.StatusFileWriter;
import twitter4j.Status;

import java.util.concurrent.BlockingQueue;

public class StatusTopicConsumer implements Runnable {

    private final String topic;
    private final BlockingQueue<Status> queue;
    private final StatusFileWriter fileWriter;

    public StatusTopicConsumer(String topic, BlockingQueue<Status> queue) {
        this.topic = topic;
        this.queue = queue;
        this.fileWriter = new StatusFileWriter(topic);
    }

    @Override
    public void run() {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println(String.format("Shutting Down Consumer for topic: '%s'...", this.topic));
            fileWriter.emptyQueueAndWriteToFile(queue);
        }));

        while(true) {

            synchronized (queue) {
                // It will only write to file when the queue is full.
                // Queue size is configurable in application.properties
                if (queue.remainingCapacity() == 0) {
                    fileWriter.emptyQueueAndWriteToFile(queue);
                }
            }
        }
    }
}
