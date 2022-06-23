package com.maesproject.gtfs.repository;

import com.maesproject.gtfs.entity.TripUpdateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TripUpdateRepository extends JpaRepository<TripUpdateEntity, Long> {

    List<TripUpdateEntity> findByTripId(String tripId);

    int deleteByIdIn(List<Long> tripUpdateIdList);

    @Transactional
    int deleteByTimestampLessThan(long timestampSeconds);
}
