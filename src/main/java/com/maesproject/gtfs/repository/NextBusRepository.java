package com.maesproject.gtfs.repository;

import com.maesproject.gtfs.util.Logger;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.List;

@Repository
public class NextBusRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public int countRoute(String routeShortName) {
        Query query = entityManager.createNativeQuery("select count(*) from routes where route_short_name = '" + routeShortName + "'");
        entityManager.close();
        return Integer.parseInt(query.getSingleResult().toString());
    }

    public int countStop(String stopCode) {
        Query query = entityManager.createNativeQuery("select count(*) from stops where stop_code = '" + stopCode + "'");
        entityManager.close();
        return Integer.parseInt(query.getSingleResult().toString());
    }

    public List<Tuple> getTripHeadSignByRoute(String routeShortName) {
        String sql = "select t.direction_id, t.trip_headsign, r.route_long_name\n" +
                "from trips t\n" +
                "join routes r on r.route_id = t.route_id\n" +
                "where r.route_short_name = '" + routeShortName + "'\n" +
                "group by t.direction_id, t.trip_headsign, r.route_long_name\n" +
                "order by t.direction_id";
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public List<Tuple> getTripHeadSignByRouteDirection(String routeShortName, int directionId) {
        String sql = "select distinct(t.trip_headsign)\n" +
                "from trips t\n" +
                "join routes r on r.route_id = t.route_id\n" +
                "where r.route_short_name = '" + routeShortName + "'\n" +
                "and t.direction_id = '" + directionId + "'";
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public List<Tuple> getStop(String routeShortName, int directionId) {
        String sql = "select s.stop_code, s.stop_name\n" +
                "from routes r\n" +
                "join trips t on t.route_id = r.route_id\n" +
                "join stop_times st on st.trip_id = t.trip_id\n" +
                "join stops s on s.stop_id = st.stop_id\n" +
                "where st.pickup_type is distinct from '1'\n" +
                "and st.drop_off_type is distinct from '1'\n" +
                "and r.route_short_name = '" + routeShortName + "'\n" +
                "and t.direction_id = '" + directionId + "'\n" +
                "group by s.stop_code, s.stop_name";
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public LocalTime getLastDepartureTime(String stopCode) {
        String sql = "select cast(max(st.departure_time) as time) last_departure_time\n" +
                "from stop_times st\n" +
                "join trips t on t.trip_id = st.trip_id\n" +
                "join routes r on r.route_id = t.route_id\n" +
                "join stops s on s.stop_id = st.stop_id\n" +
                "where st.pickup_type is distinct from '1'\n" +
                "and st.drop_off_type is distinct from '1'\n" +
                "and s.stop_code = '" + stopCode + "'";
        Query query = entityManager.createNativeQuery(sql);
        entityManager.close();
        try {
            return LocalTime.parse(query.getSingleResult().toString());
        } catch (Exception e) {
            Logger.error("Cannot find last departure time for stop '" + stopCode + "'!");
            return null;
        }
    }

    public List<Tuple> getServiceIdCalendar(String date, String day) {
        String sql = "select service_id from calendar\n" +
                "where '" + date + "' between start_date and end_date\n" +
                "and " + day + " = '1'\n" +
                "and service_id not in (\n" +
                "\tselect service_id from calendar_dates\n" +
                "\twhere date = '" + date + "'\n" +
                "\tand exception_type = '2'\n" +
                ")";
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public List<Tuple> getServiceIdCalendarDates(String date) {
        String sql = "select service_id from calendar_dates\n" +
                "where date = '" + date + "'\n" +
                "and exception_type <> '2'";
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public List<Tuple> getAllActiveServiceId(String date, String day) {
        String sql = "select service_id from calendar\n" +
                "where '" + date + "' between start_date and end_date\n" +
                "and " + day + " = '1'\n" +
                "and service_id not in (\n" +
                "\tselect service_id from calendar_dates\n" +
                "\twhere date = '" + date + "'\n" +
                "\tand exception_type = '2'\n" +
                ")\n" +
                "union\n" +
                "select service_id from calendar_dates\n" +
                "where date = '" + date + "'\n" +
                "and exception_type <> '2'";
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public List<Tuple> getTripHeadSignByRouteAndStop(String routeShortName, String stopCode) {
        String sql = "select distinct(t.trip_headsign),\n" +
                "s.stop_name, t.direction_id, r.route_long_name\n" +
                "from trips t\n" +
                "join routes r on r.route_id = t.route_id\n" +
                "join stop_times st on st.trip_id = t.trip_id\n" +
                "join stops s on s.stop_id = st.stop_id\n" +
                "where r.route_short_name = '" + routeShortName + "'\n" +
                "and s.stop_code = '" + stopCode + "'\n" +
                "order by t.trip_headsign";
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public List<Tuple> getNextDepartureByTripHeadSign(String routeShortName, String tripHeadSign, String stopCode, String serviceId, String date, String timeZone) {
        String sql = "select * from next_bus_by_trip_headsign('" + routeShortName + "', '" + tripHeadSign + "', '" + stopCode + "', array[" + serviceId + "], '" + date + "', '" + timeZone + "')\n" +
                "where rounded_minute <= 120\n" +
                "limit 6";
        System.out.println(sql + "\n");
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public List<Tuple> getNextDepartureByRoute(String routeShortName, String stopCode, String serviceId, String date, String timeZone) {
        String sql = "select * from next_bus_by_route('" + routeShortName + "', '" + stopCode + "', array[" + serviceId + "], '" + date + "', '" + timeZone + "')\n" +
                "where rounded_minute <= 120\n" +
                "limit 5";
        System.out.println(sql + "\n");
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public String getNextScheduled(String routeShortName, String tripHeadSign, String stopCode, String arrayServiceId, String date, String timezone) {
        String sql = "select to_char(st.departure_time, 'hh12:miam') as next_scheduled\n" +
                "from stop_times st\n" +
                "join trips t on t.trip_id = st.trip_id\n" +
                "join routes r on r.route_id = t.route_id\n" +
                "join stops s on s.stop_id = st.stop_id\n" +
                "where st.pickup_type is distinct from '1'\n" +
                "and st.drop_off_type is distinct from '1'\n" +
                "and r.route_short_name = '" + routeShortName + "'\n" +
                "and s.stop_code = '" + stopCode + "'\n";
        if (!tripHeadSign.isEmpty()) {
            sql += "and t.trip_headsign = '" + tripHeadSign + "'\n";
        }
        sql += "and t.service_id in (" + arrayServiceId + ") \n" +
                "and (to_date('" + date + "', 'YYYYMMDD') + st.departure_time) >= timezone('" + timezone + "', CURRENT_TIMESTAMP)\n" +
                "order by st.departure_time\n" +
                "limit 1";
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        List<Tuple> result = query.getResultList();
        for (Tuple tuple : result) {
            return tuple.get("next_scheduled").toString();
        }
        return null;
    }

    public List<Tuple> getRouteByStop(String stopCode) {
        String sql = "select distinct(r.route_long_name), r.route_short_name, s.stop_name\n" +
                "from stops s\n" +
                "join stop_times st on st.stop_id = s.stop_id\n" +
                "join trips t on t.trip_id = st.trip_id\n" +
                "join routes r on r.route_id = t.route_id\n" +
                "where s.stop_code = '" + stopCode + "'\n" +
                "order by r.route_short_name";
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public List<Tuple> getNextDeparturePerTripHeadSignWithDelay(String routeShortName, String stopCode, String tripHeadSign, String dateWithoutDash, String dayOfWeek, String timezone) {
        String sql = "select \n" +
                "extract(minute from x.diff_with_delay) + (extract(hour from x.diff_with_delay) * 60) as real_minute_with_delay,\n" +
                "\n" +
                "(case when extract(second from x.diff_with_delay) > 0 then extract(minute from x.diff_with_delay) + 1 \n" +
                " else extract(minute from x.diff_with_delay) end) + (extract(hour from x.diff_with_delay) * 60) as rounded_minute_with_delay\n" +
                "\n" +
                "from (\n" +
                "\tselect\n" +
                "\tr.route_short_name,\n" +
                "\ts.stop_code,\n" +
                "\tt.trip_headsign,\n" +
                "\tCOALESCE(stu.departure_delay, 0) as departure_delay,\n" +
                "\t(to_date('" + dateWithoutDash + "', 'YYYYMMDD') + st.departure_time) + (interval '1 seconds' * COALESCE(stu.departure_delay, 0)) as departure_date_time_with_delay,\n" +
                "\ttimezone('" + timezone + "', CURRENT_TIMESTAMP(0)) as current_date_time,\n" +
                "\t(to_date('" + dateWithoutDash + "', 'YYYYMMDD') + st.departure_time) + (interval '1 seconds' * COALESCE(stu.departure_delay, 0)) - (timezone('" + timezone + "', CURRENT_TIMESTAMP(0))) as diff_with_delay\n" +
                "\n" +
                "\tfrom stops s\n" +
                "\tjoin stop_times st on st.stop_id = s.stop_id\n" +
                "\tjoin trips t on t.trip_id = st.trip_id\n" +
                "\tjoin routes r on r.route_id = t.route_id\n" +
                "\n" +
                "\tleft join trip_updates tu on tu.trip_id = t.trip_id\n" +
                "\tleft join stop_time_updates stu on stu.trip_update_id = tu.id and stu.stop_id = s.stop_id\n" +
                "\n" +
                "\twhere st.pickup_type is distinct from '1'\n" +
                "\tand st.drop_off_type is distinct from '1'\n" +
                "\tand r.route_short_name = '" + routeShortName + "'\n" +
                "\tand s.stop_code = '" + stopCode + "'\n" +
                "\tand t.trip_headsign = '" + tripHeadSign + "'\n" +
                "\tand (\n" +
                "\t\tt.service_id in (\n" +
                "\t\t\tselect service_id from calendar \n" +
                "\t\t\twhere '" + dateWithoutDash + "' between start_date and end_date \n" +
                "\t\t\tand " + dayOfWeek + " = '1'\n" +
                "\t\t\tand service_id not in (\n" +
                "\t\t\t\tselect service_id from calendar_dates\n" +
                "\t\t\t\twhere date = '" + dateWithoutDash + "'\n" +
                "\t\t\t\tand exception_type = '2'\n" +
                "\t\t\t)\n" +
                "\t\t)\n" +
                "\t\tor\n" +
                "\t\tt.service_id in (\n" +
                "\t\t\tselect service_id from calendar_dates\n" +
                "\t\t\twhere date = '" + dateWithoutDash + "' \n" +
                "\t\t\tand exception_type <> '2'\n" +
                "\t\t)\n" +
                "\t)\n" +
                "\tand (to_date('" + dateWithoutDash + "', 'YYYYMMDD') + st.departure_time) >= timezone('" + timezone + "', CURRENT_TIMESTAMP)\n" +
                "\torder by st.departure_time\n" +
                "\tlimit 6\n" +
                ") as x\n" +
                "where (case when extract(second from x.diff_with_delay) > 0 then extract(minute from x.diff_with_delay) + 1 \n" +
                "\t   else extract(minute from x.diff_with_delay) end) + (extract(hour from x.diff_with_delay) * 60) <= 120";

        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }
}
