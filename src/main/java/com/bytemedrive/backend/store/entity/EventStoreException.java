package com.bytemedrive.backend.store.entity;

public class EventStoreException extends RuntimeException {
    public EventStoreException() {
    }

    public EventStoreException(String message) {
        super(message);
    }

    public EventStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
