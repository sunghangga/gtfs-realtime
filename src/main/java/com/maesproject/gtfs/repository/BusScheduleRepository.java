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

    public List<Tuple> getRouteAndDirectionByParam(String param, String arrayServiceId) {
        String sql = "select r.route_short_name, r.route_long_name, dne.direction_id, dne.direction_name\n" +
                "from routes r\n" +
                "join direction_names_exceptions dne on dne.route_name = r.route_short_name\n" +
                "join trips t on t.route_id = r.route_id\n" +
                "where r.route_type = '3'\n" +
                "and (\n" +
                "\tlower(route_short_name) like '%" + param.toLowerCase() + "%'\n" +
                "\tor\n" +
                "\tlower(route_long_name) like '%" + param.toLowerCase() + "%'\n" +
                ")\n" +
                "and t.service_id in (" + arrayServiceId + ")\n" +
                "group by r.route_short_name, r.route_long_name, dne.direction_id, dne.direction_name\n" +
                "order by r.route_short_name, dne.direction_id";
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public List<Tuple> getStop(String routeShortName, int directionId) {
        String sql = "select s.stop_id, s.stop_code, s.stop_name\n" +
                "from (\n" +
                "\tselect distinct(st.stop_id)\n" +
                "\tfrom stop_times st\n" +
                "\tjoin trips t on t.trip_id = st.trip_id\n" +
                "\tjoin routes r on r.route_id = t.route_id\n" +
                "\twhere r.route_short_name = '" + routeShortName + "'\n" +
                "\tand t.direction_id = '" + directionId + "'\n" +
                ") as x\n" +
                "join stops s on s.stop_id = x.stop_id";
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public List<Tuple> getArrivalTime(String routeShortName, int directionId, String arrayServiceId, String stopId, String date, String startDateTime, String endDateTime) {
        String sql = "select cast(st.arrival_time as time) as time_schedule\n" +
                "from stop_times st\n" +
                "join trips t on t.trip_id = st.trip_id\n" +
                "join routes r on r.route_id = t.route_id\n" +
                "where r.route_short_name = '" + routeShortName + "'\n" +
                "and t.direction_id = '" + directionId + "'\n" +
                "and st.stop_id = '" + stopId + "'\n" +
                "and t.service_id in (" + arrayServiceId + ")\n" +
                "and to_date('" + date + "', 'YYYY-MM-DD') + st.arrival_time between '" + startDateTime + "' and '" + endDateTime + "'\n" +
                "order by st.arrival_time";
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }

    public List<Tuple> getDirectionByRoute(String routeShortName) {
        String sql = "select direction_id, direction_name\n" +
                "from public.direction_names_exceptions\n" +
                "where route_name = '" + routeShortName + "'\n" +
                "order by direction_id";
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        entityManager.close();
        return query.getResultList();
    }
}
