package com.bytemedrive.backend.command.customer.entity;

import com.bytemedrive.backend.store.entity.AbstractAggregate;
import com.bytemedrive.backend.store.entity.RootAggregate;

import java.util.Set;


@RootAggregate(CustomerAggregate.NAME)
public class CustomerAggregate extends AbstractAggregate {
    public static final String NAME = "customer";

    public String id;

    public Set<String> events;

    @Override
    public String getAggregateId() {
        return this.id;
    }
}
