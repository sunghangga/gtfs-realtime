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

    @Query(value = "select sm from StopMonitoring sm " +
            "where sm.agencyId = :agencyId " +
            "and (:stopId is null or sm.stopId = :stopId) " +
            "and (:vehicleLabel is null or sm.vehicleLabel = :vehicleLabel) " +
            "and sm.timestamp >= :timestamp " +
            "order by sm.expectedArrivalTime, sm.vehicleLabel, sm.currentStopSequence, sm.stopSequence ")
    List<StopMonitoring> getStopMonitoring(
            @Param("agencyId") String agencyId,
            @Param("stopId") String stopId,
            @Param("vehicleLabel") String vehicleLabel,
            @Param("timestamp") long timestamp);
}
