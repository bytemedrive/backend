package com.bytemedrive.backend.store.root.entity;

import java.time.ZonedDateTime;


public record EventObjectWrapper(String eventName, String aggregateId, ZonedDateTime publishedAt, Object data) {
}
