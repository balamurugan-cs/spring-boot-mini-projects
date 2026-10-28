package com.example.Exception;

public class InvalidRechargeException extends RuntimeException {

    public InvalidRechargeException(String message) {
        super(message);
    }

}