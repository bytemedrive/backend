package com.bytemedrive.backend.store.entity;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.StringJoiner;


public record EventMapWrapper(String eventName, String aggregateId, ZonedDateTime publishedAt, Map<String, ?> data) implements Comparable<EventMapWrapper> {

    @Override
    public String toString() {
        return new StringJoiner(", ", EventMapWrapper.class.getSimpleName() + "[", "]")
                .add("aggregateId='" + aggregateId + "'")
                .add("publishedAt=" + publishedAt)
                .add("eventName='" + eventName + "'")
                .toString();
    }

    @Override
    public int compareTo(EventMapWrapper emw) {
        if (publishedAt == null && emw.publishedAt == null) {
            return 0;
        } else if (publishedAt == null) {
            return -1;
        } else if (emw.publishedAt == null) {
            return 1;
        }

        return publishedAt.compareTo(emw.publishedAt);
    }
}
