package com.fuelfinder.backend.exception;

public class ImplausiblePriceException extends RuntimeException {

    public ImplausiblePriceException(double currentPrice, double submittedPrice) {
        super(String.format(
                "Price $%.2f is too different from the current price ($%.2f). "
                        + "Prices can only change by up to $0.50 at a time -- please double check.",
                submittedPrice,
                currentPrice));
    }

    public ImplausiblePriceException(String message) {
        super(message);
    }
}
