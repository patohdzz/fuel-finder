package com.fuelfinder.backend.repository;

import com.fuelfinder.backend.model.FuelPrice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import com.fuelfinder.backend.model.FuelType;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FuelPriceRepository extends JpaRepository<FuelPrice, Long> { // talks to the database/MySQL
    List<FuelPrice> findByStation_Id(Long stationId);

    // conceptually: fuelPriceRepository.findByFuelType(FuelType.REGULAR);
    List<FuelPrice> findByFuelType(FuelType fuelType);

    // we're going to write our first custom JPA query.
    @Query("""
    SELECT fp FROM FuelPrice fp
    WHERE fp.fuelType = :fuelType
    AND fp.price = (
        SELECT MIN(fp2.price)
        FROM FuelPrice fp2
        WHERE fp2.fuelType = :fuelType
    )
    """)
    List<FuelPrice> findCheapestByFuelType(@Param("fuelType") FuelType fuelType);
    // Find the minimum price, then return every row whose price equals that minimum.

    // FuelPrice → Station → ZIP code
    @Query("""
    SELECT fp FROM FuelPrice fp
    WHERE fp.fuelType = :fuelType
    AND fp.station.zipCode = :zipCode
    AND fp.price = (
        SELECT MIN(fp2.price)
        FROM FuelPrice fp2
        WHERE fp2.fuelType = :fuelType
        AND fp2.station.zipCode = :zipCode
    )
    """)
    List<FuelPrice> findCheapestByFuelTypeAndZipCode(@Param("fuelType") FuelType fuelType, @Param("zipCode") String zipCode);

    Optional<FuelPrice> findByStation_IdAndFuelType(Long stationId, FuelType fuelType);

    List<FuelPrice> findByFuelTypeAndStation_ZipCodeOrderByPriceAsc(FuelType fuelType, String zipCode);
            
            
    
    
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