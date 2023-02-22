package com.bytemedrive.backend.command.file.entity;

import com.bytemedrive.backend.store.entity.AggregateId;
import com.bytemedrive.backend.store.entity.StoreEvent;


@StoreEvent(name = "file-uploaded", aggregate = FileAggregate.class)
public record EventFileUploaded(@AggregateId String id) {
}
