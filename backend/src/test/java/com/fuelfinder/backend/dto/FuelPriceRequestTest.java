package com.fuelfinder.backend.dto;

import com.fuelfinder.backend.model.FuelType;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// Exercises the same jakarta.validation.Validator Spring runs internally
// when a controller method is annotated with @Valid, without needing a
// full MockMvc/@WebMvcTest setup just for this one field.
class FuelPriceRequestTest {

    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Upper-bound price checks ($10 first-report ceiling, $1.00 max change)
    // now live in FuelPriceService instead of here, since they depend on
    // whether the station already has a price to compare against --
    // see FuelPriceServiceTest for those cases.

    @Test
    void priceAtOrBelowZero_failsValidation() {
        FuelPriceRequest request = new FuelPriceRequest();
        request.setPrice(0.0);
        request.setFuelType(FuelType.REGULAR);

        Set<ConstraintViolation<FuelPriceRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("Price must be greater than 0", violations.iterator().next().getMessage());
    }
}
