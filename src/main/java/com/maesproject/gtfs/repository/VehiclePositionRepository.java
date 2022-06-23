package com.maesproject.gtfs.repository;

import com.maesproject.gtfs.entity.VehiclePositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface VehiclePositionRepository extends JpaRepository<VehiclePositionEntity, Long> {

    List<VehiclePositionEntity> findByVehicleLabel(String vehicleLabel);

    List<VehiclePositionEntity> findByVehicleLabelAndRouteId(String vehicleLabel, String routeId);

    int deleteByIdIn(List<Long> vehiclePositionIdList);

    @Transactional
    int deleteByTimestampLessThan(long timestampSeconds);
}
