package com.ong.ian.twitter.writer;

import com.ong.ian.twitter.config.AppConfig;
import twitter4j.Status;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

public class StatusFileWriter {

    private String filename;

    public StatusFileWriter(String topic) {
        this.filename = topic + ".txt";
    }

    public void emptyQueueAndWriteToFile(BlockingQueue<Status> queue) {
        if (queue.size() > 0) {
            Properties properties = AppConfig.getInstance().getProperties();
            String directoryPath = properties.getProperty(AppConfig.KEY_DIR_PATH);
            String fileAbsolutePath = directoryPath + File.separator + filename;
            File file = new File(fileAbsolutePath);

            try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
                while (null != queue.peek()) {
                    Status status = queue.poll();
                    writer.println("User: " + status.getUser().getName() + " (@" + status.getUser().getScreenName() + ")");
                    writer.println("Status: " + status.getText());
                    writer.println("------------------------------------------");
                }
            } catch (FileNotFoundException e) {
                System.err.println(String.format("Error occurred when writing status to file: '%s'", this.filename));
            }
        }
    }
}
