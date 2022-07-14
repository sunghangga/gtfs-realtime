package com.maesproject.gtfs.repository;

import com.maesproject.gtfs.entity.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<AlertEntity, Long> {

    List<AlertEntity> findByEntityId(String entityId);

    int deleteByIdIn(List<Long> alertIdList);

    @Transactional
    int deleteByTimestampLessThan(long timestampSeconds);
}
