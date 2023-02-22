package com.bytemedrive.backend.store.entity;

import java.util.List;


public record EventStream(String aggregateId, List<?> events) {

}
