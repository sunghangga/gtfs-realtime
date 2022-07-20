package com.maesproject.gtfs.repository;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.Tuple;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
public class NextBusRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Tuple> getRouteByParam(String param, String arrayServiceId) {
        String sql = "select distinct(r.route_short_name), r.route_long_name\n" +
                "from routes r\n" +
                "join trips t on t.route_id = r.route_id\n" +
                "where r.route_type = '3'\n" +
                "and (\n" +
                "\tlower(route_short_name) like '%" + param.toLowerCase() + "%'\n" +
                "\tor\n" +
                "\tlower(route_long_name) like '%" + param.toLowerCase() + "%'\n" +
                ")\n" +
                "and t.service_id in (" + arrayServiceId + ")\n" +
                "order by r.route_short_name";

        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public List<Tuple> getStopByParam(String param) {
        String sql = "select stop_code, stop_name\n" +
                "from stops\n" +
                "where stop_code <> ''\n" +
                "and (\n" +
                "\tlower(stop_code) like '%" + param.toLowerCase() + "%'\n" +
                "\tor\n" +
                "\tlower(stop_name) like '%" + param.toLowerCase() + "%'\n" +
                ")\n" +
                "order by stop_code";

        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

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

    public List<Tuple> getActiveRoutes(String arrayServiceId) {
        String sql = "select distinct(r.route_short_name), r.route_long_name\n" +
                "from routes r\n" +
                "join trips t on t.route_id = r.route_id\n" +
                "where r.route_type = '3'\n" +
                "and t.service_id in (" + arrayServiceId + ")\n" +
                "order by r.route_short_name";

        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
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

    public List<Tuple> getTripHeadSignByRouteAndDirection(String routeShortName, int directionId) {
        String sql = "select distinct(t.trip_headsign)\n" +
                "from trips t\n" +
                "join routes r on r.route_id = t.route_id\n" +
                "where r.route_short_name = '" + routeShortName + "'\n" +
                "and t.direction_id = '" + directionId + "'";

        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public List<Tuple> getStopByRouteAndDirection(String routeShortName, int directionId) {
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
            return null;
        }
    }

    public List<Tuple> getAllActiveServiceId(String dateWithoutDash, String dayOfWeek) {
        String sql = "select distinct(t.service_id)\n" +
                "from trips t\n" +
                "join routes r on r.route_id = t.route_id\n" +
                "where r.route_type = '3'\n" +
                "and t.service_id in (\n" +
                "\tselect service_id from calendar\n" +
                "\twhere '" + dateWithoutDash + "' between start_date and end_date\n" +
                "\tand " + dayOfWeek + " = '1'\n" +
                "\tand service_id not in (\n" +
                "\t\tselect service_id from calendar_dates\n" +
                "\t\twhere date = '" + dateWithoutDash + "'\n" +
                "\t\tand exception_type = '2'\n" +
                "\t)\n" +
                "\tunion\n" +
                "\tselect service_id from calendar_dates\n" +
                "\twhere date = '" + dateWithoutDash + "'\n" +
                "\tand exception_type = '1'\n" +
                ")";

        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public List<Tuple> getTripHeadSignByRouteAndStop(String routeShortName, String stopCode, String arrayServiceId) {
        String sql = "select distinct(t.trip_headsign), s.stop_name, t.direction_id, r.route_long_name\n" +
                "from trips t\n" +
                "join routes r on r.route_id = t.route_id\n" +
                "join stop_times st on st.trip_id = t.trip_id\n" +
                "join stops s on s.stop_id = st.stop_id\n" +
                "where st.pickup_type is distinct from '1'\n" +
                "and st.drop_off_type is distinct from '1'\n" +
                "and r.route_short_name = '" + routeShortName + "'\n" +
                "and s.stop_code = '" + stopCode + "'\n" +
                "and t.service_id in (" + arrayServiceId + ")\n" +
                "order by t.trip_headsign";

        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public List<Tuple> getNextDeparturePerTripHeadSignUnion(String routeShortName, String stopCode, String tripHeadSign, String[] arrayServiceId, String[] arrayDateWithoutDash, String timeZone) {
        String sql = "" +
                "select * from next_bus_per_trip_head_sign('" + routeShortName + "', '" + stopCode + "', '" + tripHeadSign + "', array[" + arrayServiceId[0] + "], '" + arrayDateWithoutDash[0] + "', '" + timeZone + "')\n" +
                "where rounded_minute <= 120\n" +
                "union\n" +
                "select * from next_bus_per_trip_head_sign('" + routeShortName + "', '" + stopCode + "', '" + tripHeadSign + "', array[" + arrayServiceId[1] + "], '" + arrayDateWithoutDash[1] + "', '" + timeZone + "')\n" +
                "where rounded_minute <= 120\n" +
                "order by rounded_minute\n" +
                "limit 6";

        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public List<Tuple> getRouteByStop(String stopCode) {
        String sql = "select r.route_short_name, r.route_long_name, s.stop_name\n" +
                "from stops s\n" +
                "join stop_times st on st.stop_id = s.stop_id\n" +
                "join trips t on t.trip_id = st.trip_id\n" +
                "join routes r on r.route_id = t.route_id\n" +
                "where s.stop_code = '" + stopCode + "'\n" +
                "group by r.route_long_name, r.route_short_name, s.stop_name\n" +
                "order by r.route_short_name";

        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public List<Tuple> getNextDeparturePerRouteUnion(String routeShortName, String stopCode, String[] arrayServiceId, String[] arrayDateWithoutDash, String timeZone) {
        String sql = "" +
                "select * from next_bus_per_route('" + routeShortName + "', '" + stopCode + "', array[" + arrayServiceId[0] + "], '" + arrayDateWithoutDash[0] + "', '" + timeZone + "')\n" +
                "where rounded_minute <= 120\n" +
                "union\n" +
                "select * from next_bus_per_route('" + routeShortName + "', '" + stopCode + "', array[" + arrayServiceId[1] + "], '" + arrayDateWithoutDash[1] + "', '" + timeZone + "')\n" +
                "where rounded_minute <= 120\n" +
                "order by rounded_minute\n" +
                "limit 5";

        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public LocalDateTime getNextScheduledAfterLastTrip(String routeShortName, String tripHeadSign, String stopCode, String arrayServiceId, String dateWithoutDash, String timeZone, String lastDepartureDateTime) {
        String sql = "select (to_date('" + dateWithoutDash + "', 'YYYYMMDD') + st.departure_time) as next_scheduled\n" +
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
                "and (to_date('" + dateWithoutDash + "', 'YYYYMMDD') + st.departure_time) >= timezone('" + timeZone + "', CURRENT_TIMESTAMP)\n";

        if (!lastDepartureDateTime.isEmpty()) {
            sql += "and (to_date('" + dateWithoutDash + "', 'YYYYMMDD') + st.departure_time) > '" + lastDepartureDateTime + "'\n";
        }

        sql += "order by st.departure_time\n" +
                "limit 1";

        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        List<Tuple> result = query.getResultList();
        for (Tuple tuple : result) {
            return LocalDateTime.parse(tuple.get("next_scheduled").toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
        }
        return null;
    }

    public List<Tuple> getStopInfo(String stopCode) {
        String sql = "select stop_name, stop_lat, stop_lon\n" +
                "from stops\n" +
                "where stop_code = '" + stopCode + "'";

        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public List<Tuple> getMapDepartureTimeByStopUnion(String stopCode, String routeShortName, String[] arrayServiceId, String[] arrayDateWithoutDash, String timeZone) {
        String sql = "" +
                "select * from next_bus_map_by_stop('" + stopCode + "', array[" + arrayServiceId[0] + "], '" + arrayDateWithoutDash[0] + "', '" + timeZone + "')\n" +
                "where rounded_minute <= 120\n" +
                "union\n" +
                "select * from next_bus_map_by_stop('" + stopCode + "', array[" + arrayServiceId[1] + "], '" + arrayDateWithoutDash[1] + "', '" + timeZone + "')\n" +
                "where rounded_minute <= 120\n";
        if (!routeShortName.isEmpty()) {
            sql += "and route_short_name = '" + routeShortName + "'\n";
        }
        sql += "order by route_short_name, rounded_minute";

        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public List<Tuple> getMapVehicle(String stopCode, String routeShortName, String timeZone) {
        String sql = "select r.route_short_name, t.trip_headsign, vp.vehicle_label, vp.position_latitude, vp.position_longitude, \n" +
                "timezone('" + timeZone + "', to_timestamp(vp.timestamp)) as \"timestamp\"\n" +
                "from vehicle_positions vp\n" +
                "join routes r on r.route_id = vp.route_id\n" +
                "join trips t on t.trip_id = vp.trip_id\n" +
                "where vp.trip_id in (\n" +
                "\tselect distinct(t.trip_id)\n" +
                "\tfrom trips t\n" +
                "\tjoin stop_times st on st.trip_id = t.trip_id\n" +
                "\tjoin stops s on s.stop_id = st.stop_id\n" +
                "\tjoin routes r on r.route_id = t.route_id\n" +
                "\twhere r.route_type = '3'\n";

        if (!stopCode.isEmpty()) {
            sql += "\tand s.stop_code = '" + stopCode + "'\n";
        }

        if (!routeShortName.isEmpty()) {
            sql += "\tand r.route_short_name = '" + routeShortName + "'\n";
        }

        sql += ")\n" +
                "order by r.route_short_name, t.trip_headsign, vp.timestamp";

        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public List<Tuple> getMapRoutePath(String stopCode, String routeShortName) {
        String sql = "select r.route_short_name, sh.shape_id, sh.shape_pt_sequence, sh.shape_pt_lat, sh.shape_pt_lon, sh.shape_dist_traveled\n" +
                "from shapes sh\n" +
                "join trips t on t.shape_id = sh.shape_id\n" +
                "join routes r on r.route_id = t.route_id\n" +
                "join stop_times st on st.trip_id = t.trip_id\n" +
                "join stops s on s.stop_id = st.stop_id\n" +
                "where r.route_short_name = '" + routeShortName + "'\n";
        if (!stopCode.isEmpty()) {
            sql += "and s.stop_code = '" + stopCode + "'\n";
        }
        sql += "group by r.route_short_name, sh.shape_id, sh.shape_pt_sequence, sh.shape_pt_lat, sh.shape_pt_lon, sh.shape_dist_traveled\n" +
                "order by r.route_short_name, sh.shape_id, sh.shape_pt_sequence";

        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

}
