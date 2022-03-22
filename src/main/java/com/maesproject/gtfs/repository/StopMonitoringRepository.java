package com.maesproject.gtfs.repository;

import com.maesproject.gtfs.compositeid.StopMonitoringCompositeId;
import com.maesproject.gtfs.entity.StopMonitoring;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StopMonitoringRepository extends JpaRepository<StopMonitoring, StopMonitoringCompositeId> {

    @Query(value = "select sm from StopMonitoring sm where sm.agencyId = :agencyId and (:stopId is null or sm.stopId = :stopId) " +
            "and (to_date(sm.tripStartDate, 'YYYYMMDD') + sm.lastArrivalTime) >= timezone(:timeZone, CURRENT_TIMESTAMP) ")
    List<StopMonitoring> getStopMonitoring(@Param("timeZone") String timeZone, @Param("agencyId") String agencyId, @Param("stopId") String stopId);
}
