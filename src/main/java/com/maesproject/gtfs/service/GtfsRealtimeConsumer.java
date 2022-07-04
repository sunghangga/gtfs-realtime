package com.maesproject.gtfs.service;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.maesproject.gtfs.util.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Service
public class GtfsRealtimeConsumer {
    @Autowired
    private InitializeManager initializeManager;

    public long consume(String feedUrl, String type, long lastTimestamp) {
        try {
            FeedMessage feed;
            try {
                URL url = new URL(feedUrl);
                feed = FeedMessage.parseFrom(url.openStream());
                if (feed.getEntityList().isEmpty()) return lastTimestamp;
            } catch (IOException e) {
//                Logger.error("Error while parsing GTFS data from " + feedUrl + "! " + e.getMessage());
                return lastTimestamp;
            }

            // check timestamp
            if (feed.getHeader().hasTimestamp()) {
                if (feed.getHeader().getTimestamp() == lastTimestamp) return lastTimestamp;
                else lastTimestamp = feed.getHeader().getTimestamp();
            }

            // check header detail
            if (!feed.getHeader().getIncrementality().toString().equals("FULL_DATASET")) {
                Logger.warn("Feed " + feedUrl + " 'incrementality' is " + feed.getHeader().getIncrementality());
            }

            String[] gtfsVersion = {"1.0", "2.0"};
            List<String> gtfsVersionList = Arrays.asList(gtfsVersion);
            if (!gtfsVersionList.contains(feed.getHeader().getGtfsRealtimeVersion())) {
                Logger.warn("Feed " + feedUrl + " is using GTFS-Realtime version " + feed.getHeader().getGtfsRealtimeVersion());
            }

            // process data
            initializeManager.initializeData(feed, feedUrl, type);

        } catch (Exception e) {
            Logger.error("Error consuming GTFS feed from " + feedUrl + "! " + e.getMessage());
        }
        return lastTimestamp;
    }

}
