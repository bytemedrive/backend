package com.bytemedrive.backend.command.file.control;

import com.bytemedrive.backend.command.customer.entity.CustomerAggregate;
import com.bytemedrive.backend.command.file.entity.EventFileUploaded;
import com.bytemedrive.backend.command.file.entity.FileAggregate;
import com.bytemedrive.backend.store.root.boundary.RootAggregateConverter;
import com.bytemedrive.backend.store.root.entity.EventStream;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


@ApplicationScoped
public class FileConverter implements RootAggregateConverter<FileAggregate> {

    @Inject
    Logger log;

    @Override
    public FileAggregate convert(EventStream eventStream) {
        var file = new FileAggregate();
        for (var event : eventStream.events()) {
            try {
                switch (event) {
                    case EventFileUploaded e -> applyEvent(e, file);
                    default -> applyUnknown(event);
                }

            } catch (Exception e) {
                log.errorf(e, "Cannot apply event: %s", event);
            }
        }
        return file;
    }

    private void applyEvent(EventFileUploaded event, FileAggregate file) {
        file.id = event.id();
    }

    private void applyUnknown(Object event) {
        log.debugf("No handling for event: %s", event.getClass());
    }

    @Override
    public Class getAggregateClass() {
        return CustomerAggregate.class;
    }
}
