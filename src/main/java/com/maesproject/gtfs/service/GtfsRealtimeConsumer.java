package com.maesproject.gtfs.service;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.maesproject.gtfs.util.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;

@Service
public class GtfsRealtimeConsumer {
    @Autowired
    private InitializeManager initializeManager;
    @Value("${consume.seconds.interval}")
    private int consumeInterval;

    public void consumeFeed(String feedUrl) {
        Logger.info("Consuming data from feeder " + feedUrl);
        long lastTimestamp = 0;
        boolean loop = true;
        while (loop) {
            FeedMessage feed;
            try {
                URL url = new URL(feedUrl);
                feed = FeedMessage.parseFrom(url.openStream());
            } catch (IOException e) {
                int delay = 10;
                Logger.error(e.getMessage());
                Logger.info("Reconnecting to " + feedUrl + " in " + delay + " seconds");
                wait(delay);
                continue;
            }

            if (feed.getHeader().hasTimestamp()) {
                // check if data is new
                if (lastTimestamp != feed.getHeader().getTimestamp()) {
                    lastTimestamp = feed.getHeader().getTimestamp();

                    // check header detail
                    if (!feed.getHeader().getIncrementality().toString().equals("FULL_DATASET")) {
                        Logger.warn("Feed " + feedUrl + " 'incrementality' is " + feed.getHeader().getIncrementality());
                    }
                    if (!feed.getHeader().getGtfsRealtimeVersion().contains("1.0")) {
                        Logger.warn("Feed " + feedUrl + " is using GTFS-Realtime version " + feed.getHeader().getGtfsRealtimeVersion());
                    }

                    try {
                        boolean result = initializeManager.processData(feed, feedUrl);
                        if (!result) loop = false;
                    } catch (Exception e) {
                        Logger.error(e.getMessage());
                    }
                }
            }

            if (loop) {
                wait(consumeInterval);
            }
        }
        Logger.warn("Stop consuming data from feeder " + feedUrl);
    }

    public void wait(int seconds) {
        try {
            Thread.sleep(1000L * seconds);
        } catch (InterruptedException e) {
            Logger.error(e.getMessage());
        }
    }
}
