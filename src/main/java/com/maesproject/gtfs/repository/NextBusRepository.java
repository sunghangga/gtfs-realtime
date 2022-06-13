package com.maesproject.gtfs.repository;

import org.springframework.stereotype.Repository;

import javax.persistence.*;
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

    public List<Tuple> getTripHeadSign(String routeShortName) {
        Query query = entityManager.createNativeQuery(queryTripHeadSign(routeShortName), Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public String queryTripHeadSign(String routeShortName) {
        return "select t.direction_id, t.trip_headsign, r.route_long_name\n" +
                "from trips t\n" +
                "join routes r on r.route_id = t.route_id\n" +
                "where r.route_short_name = '" + routeShortName + "'\n" +
                "group by t.direction_id, t.trip_headsign, r.route_long_name\n" +
                "order by t.direction_id";
    }

    public List<Tuple> getTripHeadSignByDirection(String routeShortName, int directionId) {
        Query query = entityManager.createNativeQuery(queryTripHeadSignByDirection(routeShortName, directionId), Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public String queryTripHeadSignByDirection(String routeShortName, int directionId) {
        return "select distinct(t.trip_headsign)\n" +
                "from trips t\n" +
                "join routes r on r.route_id = t.route_id\n" +
                "where r.route_short_name = '" + routeShortName + "'\n" +
                "and t.direction_id = '" + directionId + "'";
    }

    public List<Tuple> getStop(String routeShortName, int directionId) {
        Query query = entityManager.createNativeQuery(queryGetStop(routeShortName, directionId), Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public String queryGetStop(String routeShortName, int directionId) {
        return "select s.stop_code, s.stop_name\n" +
                "from routes r \n" +
                "join trips t on t.route_id = r.route_id\n" +
                "join stop_times st on st.trip_id = t.trip_id\n" +
                "join stops s on s.stop_id = st.stop_id\n" +
                "where st.pickup_type <> '1'\n" +
                "and st.drop_off_type <> '1'\n" +
                "and r.route_short_name = '" + routeShortName + "'\n" +
                "and t.direction_id = '" + directionId + "'\n" +
                "group by s.stop_code, s.stop_name";
    }

    public List<Tuple> getLastDeparture(String routeShortName, String stopCode) {
        Query query = entityManager.createNativeQuery(queryLastDeparture(routeShortName, stopCode), Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public String queryLastDeparture(String routeShortName, String stopCode) {
        return "select cast(max(st.departure_time) as time) departure_time\n" +
                "from stop_times st \n" +
                "join trips t on t.trip_id = st.trip_id \n" +
                "join routes r on r.route_id = t.route_id \n" +
                "join stops s on s.stop_id = st.stop_id \n" +
                "where st.pickup_type <> '1' \n" +
                "and st.drop_off_type <> '1' \n" +
                "and r.route_short_name = '" + routeShortName + "' \n" +
                "and s.stop_code = '" + stopCode + "'";
    }

    public List<Tuple> getServiceIdCalendar(String date, String day) {
        Query query = entityManager.createNativeQuery(queryServiceIdCalendar(date, day), Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public String queryServiceIdCalendar(String date, String day) {
        return "select service_id from calendar \n" +
                "where '" + date + "' between start_date and end_date \n" +
                "and " + day + " = '1'";
    }

    public List<Tuple> getServiceIdCalendarDates(String date) {
        Query query = entityManager.createNativeQuery(queryServiceIdCalendarDates(date), Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public String queryServiceIdCalendarDates(String date) {
        return "select service_id from calendar_dates \n" +
                "where date = '" + date + "' \n" +
                "and exception_type <> '2'";
    }

    public List<Tuple> getTripHeadSignByStop(String routeShortName, String stopCode) {
        Query query = entityManager.createNativeQuery(queryTripHeadSignByStop(routeShortName, stopCode), Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public String queryTripHeadSignByStop(String routeShortName, String stopCode) {
        return "select distinct(t.trip_headsign)\n" +
                "from trips t\n" +
                "join routes r on r.route_id = t.route_id\n" +
                "join stop_times st on st.trip_id = t.trip_id\n" +
                "join stops s on s.stop_id = st.stop_id\n" +
                "where r.route_short_name = '" + routeShortName + "'\n" +
                "and s.stop_code = '" + stopCode + "'\n" +
                "order by t.trip_headsign";
    }

    public List<Tuple> getNextDeparture(String routeShortName, String tripHeadSign, String stopCode, String serviceId, String date, String timeZone) {
        Query query = entityManager.createNativeQuery(queryNextDeparture(routeShortName, tripHeadSign, stopCode, serviceId, date, timeZone), Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public String queryNextDeparture(String routeShortName, String tripHeadSign, String stopCode, String serviceId, String date, String timeZone) {
        return "select * from next_bus('" + routeShortName + "', '" + tripHeadSign + "', '" + stopCode + "', array[" + serviceId + "], '" + date + "', '" + timeZone + "')";
    }

    public List<Tuple> getNextScheduled(String routeShortName, String tripHeadSign, String stopCode, String date, String day) {
        Query query = entityManager.createNativeQuery(queryNextScheduled(routeShortName, tripHeadSign, stopCode, date, day), Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public String queryNextScheduled(String routeShortName, String tripHeadSign, String stopCode, String date, String day) {
        return "select cast(min(st.departure_time) as character varying) next_scheduled\n" +
                "from stop_times st \n" +
                "join trips t on t.trip_id = st.trip_id \n" +
                "join routes r on r.route_id = t.route_id \n" +
                "join stops s on s.stop_id = st.stop_id \n" +
                "where st.pickup_type <> '1' \n" +
                "and st.drop_off_type <> '1' \n" +
                "and r.route_short_name = '" + routeShortName + "' \n" +
                "and s.stop_code = '" + stopCode + "' \n" +
                "and t.trip_headsign = '" + tripHeadSign + "' \n" +
                "and ( \n" +
                "\tt.service_id in ( \n" +
                "\t\tselect service_id from calendar \n" +
                "\t\twhere '" + date + "' between start_date and end_date \n" +
                "\t\tand " + day + " = '1' \n" +
                "\t) \n" +
                "\tor \n" +
                "\tt.service_id in ( \n" +
                "\t\tselect service_id from calendar_dates \n" +
                "\t\twhere date = '" + date + "' \n" +
                "\t\tand exception_type <> '2' \n" +
                "\t) \n" +
                ")";
    }
}
