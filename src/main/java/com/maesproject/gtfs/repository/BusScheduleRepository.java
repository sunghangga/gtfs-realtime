package com.maesproject.gtfs.repository;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.Tuple;
import java.util.List;

@Repository
public class BusScheduleRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public List<Tuple> getStop(String routeShortName, int directionId) {
        Query query = entityManager.createNativeQuery(queryStop(routeShortName, directionId), Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public String queryStop(String routeShortName, int directionId) {
        return "select s.stop_id, s.stop_code, s.stop_name \n" +
                "from ( \n" +
                "\tselect distinct(st.stop_id) \n" +
                "\tfrom stop_times st \n" +
                "\tjoin trips t on t.trip_id = st.trip_id \n" +
                "\tjoin routes r on r.route_id = t.route_id \n" +
                "\twhere r.route_short_name = '" + routeShortName + "' \n" +
                "\tand t.direction_id = '" + directionId + "' \n" +
                ") as x \n" +
                "join stops s on s.stop_id = x.stop_id";
    }

    public List<Tuple> getArrivalTime(String routeShortName, int directionId, String arrayServiceId, String stopId, String date, String startDateTime, String endDateTime) {
        Query query = entityManager.createNativeQuery(queryArrivalTime(routeShortName, directionId, arrayServiceId, stopId, date, startDateTime, endDateTime), Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public String queryArrivalTime(String routeShortName, int directionId, String arrayServiceId, String stopId, String date, String startDateTime, String endDateTime) {
        return "select to_char(st.arrival_time, 'hh:mi:ss') format_12_hour \n" +
                "from stop_times st \n" +
                "join trips t on t.trip_id = st.trip_id \n" +
                "join routes r on r.route_id = t.route_id \n" +
                "where r.route_short_name = '" + routeShortName + "' \n" +
                "and t.direction_id = '" + directionId + "' \n" +
                "and st.stop_id = '" + stopId + "' \n" +
                "and t.service_id in (" + arrayServiceId + ") \n" +
                "and to_date('" + date + "', 'YYYY-MM-DD') + st.arrival_time between '" + startDateTime + "' and '" + endDateTime + "' \n" +
                "order by st.arrival_time";
    }
}
