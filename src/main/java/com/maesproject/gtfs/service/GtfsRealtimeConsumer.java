package com.maesproject.gtfs.service;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.maesproject.gtfs.util.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Service
public class GtfsRealtimeConsumer {
    @Autowired
    private InitializeManager initializeManager;

    @Value("${consume.realtime.seconds.interval}")
    private int consumeInterval;

    public void consumeFeedOnce(String feedUrl) {
        String[] gtfsVersion = {"1.0", "2.0"};
        List<String> gtfsVersionList = Arrays.asList(gtfsVersion);
        try {
            URL url = new URL(feedUrl);
            FeedMessage feed = FeedMessage.parseFrom(url.openStream());
            if (feed.getHeader().hasTimestamp()) {
                // check header detail
                if (!feed.getHeader().getIncrementality().toString().equals("FULL_DATASET")) {
                    Logger.warn("Feed " + feedUrl + " 'incrementality' is " + feed.getHeader().getIncrementality());
                }
                if (!gtfsVersionList.contains(feed.getHeader().getGtfsRealtimeVersion())) {
                    Logger.warn("Feed " + feedUrl + " is using GTFS-Realtime version " + feed.getHeader().getGtfsRealtimeVersion());
                }
                // process data
                initializeManager.processData(feed, feedUrl);
            }
        } catch (IOException e) {
            Logger.error(e.getMessage());
        }
    }

    public void consumeFeed(String feedUrl) {
        Logger.info("Consuming data from feeder " + feedUrl);
        String[] gtfsVersion = {"1.0", "2.0"};
        List<String> gtfsVersionList = Arrays.asList(gtfsVersion);
        long lastTimestamp = 0;
        boolean loop = true;

        while (loop) {
            FeedMessage feed;
            try {
                URL url = new URL(feedUrl);
                feed = FeedMessage.parseFrom(url.openStream());
            } catch (IOException e) {
                int delay = 10;
                Logger.error("Error while getting feed data! " + e.getMessage());
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
                    if (!gtfsVersionList.contains(feed.getHeader().getGtfsRealtimeVersion())) {
                        Logger.warn("Feed " + feedUrl + " is using GTFS-Realtime version " + feed.getHeader().getGtfsRealtimeVersion());
                    }

                    // process data
                    try {
                        boolean result = initializeManager.processData(feed, feedUrl);
                        if (!result) loop = false;
                    } catch (Exception e) {
                        Logger.error(e.getMessage());
                    }
                }
            }

            if (loop) wait(consumeInterval);
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
