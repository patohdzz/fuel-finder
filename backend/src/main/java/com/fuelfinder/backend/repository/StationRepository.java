package com.fuelfinder.backend.repository;

import com.fuelfinder.backend.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;


// talks to the database
public interface StationRepository extends JpaRepository<Station, Long> {
    // “Spring, please create a database helper for the Station entity, where the ID is a Long.”
}