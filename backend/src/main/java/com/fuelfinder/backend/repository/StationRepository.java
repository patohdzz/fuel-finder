package com.fuelfinder.backend.repository;

import com.fuelfinder.backend.model.Station;

import javax.swing.Spring;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


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