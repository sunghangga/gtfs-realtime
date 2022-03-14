package com.maesproject.gtfs.repository;

import com.maesproject.gtfs.entity.EntitySelectorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntitySelectorRepository extends JpaRepository<EntitySelectorEntity, Integer> {
}
