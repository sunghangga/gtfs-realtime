package com.maesproject.gtfs.repository;

import com.maesproject.gtfs.entity.StopTimeUpdateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StopTimeUpdateRepository extends JpaRepository<StopTimeUpdateEntity, Integer> {
}
