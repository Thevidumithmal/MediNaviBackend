package com.example.pharmacybackend.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
