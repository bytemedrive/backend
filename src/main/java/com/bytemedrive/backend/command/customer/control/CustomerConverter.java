package com.bytemedrive.backend.command.customer.control;

import com.bytemedrive.backend.command.customer.entity.CustomerAggregate;
import com.bytemedrive.backend.command.customer.entity.EncryptedEventPublished;
import com.bytemedrive.backend.store.root.boundary.RootAggregateConverter;
import com.bytemedrive.backend.store.root.entity.EventStream;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


@ApplicationScoped
public class CustomerConverter implements RootAggregateConverter<CustomerAggregate> {

    @Inject
    Logger log;

    @Override
    public CustomerAggregate convert(EventStream eventStream) {
        var customer = new CustomerAggregate();
        for (var event : eventStream.events()) {
            try {
                switch (event) {
                    case EncryptedEventPublished e -> applyEvent(e, customer);
                    default -> applyUnknown(event);
                }

            } catch (Exception e) {
                log.errorf(e, "Cannot apply event: %s", event);
            }
        }
        return customer;
    }

    private void applyEvent(EncryptedEventPublished event, CustomerAggregate customer) {
        customer.id = event.customerId();
        if (customer.events == null) {
            customer.events = new java.util.HashSet<>();
        }

        customer.events.add(event.dataBase64());
    }

    private void applyUnknown(Object event) {
        log.debugf("No handling for event: %s", event.getClass());
    }

    @Override
    public Class getAggregateClass() {
        return CustomerAggregate.class;
    }
}
