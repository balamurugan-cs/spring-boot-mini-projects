package com.example.Exception;

public class RechargeNotFoundException extends RuntimeException {

    public RechargeNotFoundException(String message) {
        super(message);
    }

}