package com.example.Exception;

public class InvalidTicketException extends RuntimeException {

    public InvalidTicketException(String message) {
        super(message);
    }

}