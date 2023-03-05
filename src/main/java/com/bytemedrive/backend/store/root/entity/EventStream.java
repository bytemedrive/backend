package com.bytemedrive.backend.store.root.entity;

import java.util.List;


public record EventStream(String aggregateId, List<?> events) {

}
