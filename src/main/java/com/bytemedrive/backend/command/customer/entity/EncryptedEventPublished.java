package com.bytemedrive.backend.command.customer.entity;


import com.bytemedrive.backend.store.root.entity.AggregateId;
import com.bytemedrive.backend.store.root.entity.StoreEvent;


@StoreEvent(name = "encrypted-event-published", aggregate = CustomerAggregate.class)
public record EncryptedEventPublished(@AggregateId String customerId, String dataBase64) {
}
