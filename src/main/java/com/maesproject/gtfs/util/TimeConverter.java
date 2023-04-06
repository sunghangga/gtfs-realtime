package com.maesproject.gtfs.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class TimeConverter {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static String currentZoneTime(String timeZone) {
        return LocalDateTime.now(ZoneId.of(timeZone)).format(formatter);
    }

    public static String convertTripDate(String tripDate) {
        if (tripDate == null) return null;
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate localDate = LocalDate.parse(tripDate, dateFormat);
        return localDate.toString();
    }

    public static String durationToZoneTime(Duration duration, String gtfsDate) {
        if (duration == null) return null;
        if (gtfsDate == null) return null;
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate nowDate = LocalDate.parse(gtfsDate, dateFormat);
        LocalDateTime localDateTime = nowDate.atStartOfDay().plus(duration);
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(localDateTime.format(formatter));
        return zonedDateTime.toString();
    }

    public static String unixToDateTime(String timeZone, long unixTime) {
        if (unixTime <= 0) return "";
        Instant instant = Instant.ofEpochSecond(unixTime);
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(ZonedDateTime.ofInstant(instant, ZoneId.of(timeZone)).format(formatter));
        return zonedDateTime.toString();
    }

    public long currentTimeToUnix(String timeZone) {
        return LocalDateTime.now(ZoneId.of(timeZone)).atZone(ZoneId.of(timeZone)).toEpochSecond();
    }
}
