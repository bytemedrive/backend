package com.bytemedrive.backend.store.entity;

import java.time.ZonedDateTime;


public record EventObjectWrapper(String eventName, String aggregateId, ZonedDateTime publishedAt, Object data) {
}
