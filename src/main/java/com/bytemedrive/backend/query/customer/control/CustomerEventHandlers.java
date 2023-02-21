package com.bytemedrive.backend.query.customer.control;


import com.bytemedrive.backend.command.customer.entity.CustomerAggregate;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Objects;
import java.util.stream.Collectors;


@ApplicationScoped
public class CustomerEventHandlers {

    @Inject
    EntityManager entityManager;

    @Transactional
    void onChange(@ObservesAsync CustomerAggregate aggregate) {
        var customer = Objects.requireNonNullElse(entityManager.find(CustomerEntity.class, aggregate.id), new CustomerEntity());
        customer.id = aggregate.id;
        customer.events = aggregate.events.stream().toList();

        entityManager.merge(customer);
    }
}
