package com.maesproject.gtfs.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DurationCounter {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean longFormat = true;

    public DurationCounter() {
    }

    public DurationCounter(LocalDateTime startTime, LocalDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public DurationCounter(LocalDateTime startTime, LocalDateTime endTime, boolean longFormat) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.longFormat = longFormat;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public boolean isLongFormat() {
        return longFormat;
    }

    public void setLongFormat(boolean longFormat) {
        this.longFormat = longFormat;
    }

    public String getDuration() {
        // set time value for every time unit
        LocalDateTime tempDateTime = LocalDateTime.from(this.startTime);
        long days = tempDateTime.until(this.endTime, ChronoUnit.DAYS);
        tempDateTime = tempDateTime.plusDays(days);
        long hours = tempDateTime.until(this.endTime, ChronoUnit.HOURS);
        tempDateTime = tempDateTime.plusHours(hours);
        long minutes = tempDateTime.until(this.endTime, ChronoUnit.MINUTES);
        tempDateTime = tempDateTime.plusMinutes(minutes);
        long seconds = tempDateTime.until(this.endTime, ChronoUnit.SECONDS);
        tempDateTime = tempDateTime.plusSeconds(seconds);
        long millis = tempDateTime.until(this.endTime, ChronoUnit.MILLIS);

        // set date format (long or short)
        String df = this.longFormat ? (days > 1 ? " days" : " day") : "d";
        String hf = this.longFormat ? (hours > 1 ? " hours" : " hour") : "h";
        String mf = this.longFormat ? (minutes > 1 ? " minutes" : " minute") : "m";
        String sf = this.longFormat ? (seconds > 1 ? " seconds" : " second") : "s";
        String msf = this.longFormat ? (millis > 1 ? " milliseconds" : " millisecond") : "ms";

        // filter zero value
        String day = days > 0 ? days + df : "";
        String hour = hours > 0 ? hours + hf : "";
        String minute = minutes > 0 ? minutes + mf : "";
        String second = seconds > 0 ? seconds + sf : "";
        String milli = millis + msf;

        // collect non zero value
        List<String> nonZeroTime = new ArrayList<>();
        if (day.isEmpty() && hour.isEmpty() && minute.isEmpty() && second.isEmpty()) {
            return milli;
        } else {
            if (!day.isEmpty()) nonZeroTime.add(day);
            if (!hour.isEmpty()) nonZeroTime.add(hour);
            if (!minute.isEmpty()) nonZeroTime.add(minute);
            if (!second.isEmpty()) nonZeroTime.add(second);
        }

        // combine value in one line
        StringBuilder duration = new StringBuilder();
        int size = nonZeroTime.size();
        for (int i = 0; i < size; i++) {
            String delimiter = i < (size - 1) ? ", " : " and ";
            duration.append((duration.length() == 0) ? nonZeroTime.get(i) : delimiter + nonZeroTime.get(i));
        }
        return duration.toString();
    }
}
