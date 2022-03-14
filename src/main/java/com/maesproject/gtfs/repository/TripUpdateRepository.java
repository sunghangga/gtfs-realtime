package com.maesproject.gtfs.repository;

import com.maesproject.gtfs.entity.TripUpdateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TripUpdateRepository extends JpaRepository<TripUpdateEntity, Integer> {

    TripUpdateEntity findByTripIdAndRouteIdAndDirectionIdAndTripStartDateAndTripStartTime(String tripId, String routeId, int directionId, String tripStartDate, String tripStartTime);

    int deleteByIdIn(List<Integer> tripUpdateIdList);

    @Transactional
    int deleteByTimestampLessThan(long timestampSeconds);
}
