package com.fuelfinder.backend.repository;

import com.fuelfinder.backend.model.Station;

import javax.swing.Spring;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import com.fuelfinder.backend.dto.StationSearchResponse;
import com.fuelfinder.backend.model.FuelType;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


// talks to the database/MySQL
public interface StationRepository extends JpaRepository<Station, Long> { // Station is the entity being managed, Long is the type of the Station ID
    // That gives access to methods such as:
        // findAll();
        // findById();
        // save();
        // deleteById();

    // When we write:
        // stationRepository.findAll();
    // Spring eventually generates SQL similar to:
        // SELECT *
        // FROM stations;

    List<Station> findByZipCode(String zipCode);

    @Query("""
        SELECT new com.fuelfinder.backend.dto.StationSearchResponse(
            s.id,
            s.name,
            s.address,
            s.city,
            s.state,
            s.zipCode,
            s.latitude,
            s.longitude,
            fp.price,
            :fuelType,
            fp.lastUpdated
        )
        FROM Station s
        LEFT JOIN FuelPrice fp
            ON fp.station = s
            AND fp.fuelType = :fuelType
        WHERE s.zipCode = :zipCode
        ORDER BY
            CASE WHEN fp.price IS NULL THEN 1 ELSE 0 END,
            fp.price ASC
        """)
    List<StationSearchResponse> searchStationsByZipCodeAndFuelType(
            @Param("zipCode") String zipCode,
            @Param("fuelType") FuelType fuelType
    );

    Optional<Station> findByOsmTypeAndOsmId(
            String osmType,
            Long osmId
    );
    
}

// Your code:

// StationRepository
//       ↑
//       │ implements
//       │
// Spring-generated class
//       ↑
//       │ object created by Spring
//       │
// StationService receives that object