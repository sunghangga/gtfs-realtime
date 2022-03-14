package com.maesproject.gtfs.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Repository
public class GlobalRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void clearRealtimeData() {
        Query query = entityManager.createNativeQuery("truncate public.alerts, public.entity_selectors, " +
                "public.stop_time_updates, public.trip_updates, public.vehicle_positions");
        query.executeUpdate();
        entityManager.close();
    }
}
