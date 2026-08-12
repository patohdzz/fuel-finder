package com.fuelfinder.backend.repository;

import com.fuelfinder.backend.model.FuelPrice;
import javax.swing.Spring;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FuelPriceRepository extends JpaRepository<FuelPrice, Long> {
    List<FuelPrice> findByStation_Id(Long stationId);
}    

// Conceptually, Spring does this:

    // FuelPriceRepository interface
    //         ↓
    // Spring Data JPA sees it
    //         ↓
    // Spring creates an implementation
    //         ↓
    // Spring creates an object from that implementation
    //         ↓
    // we inject/use that object

    // So later we'll be able to write: fuelPriceRepository.findAll();
    // even though there is no findAll() method written inside your file.