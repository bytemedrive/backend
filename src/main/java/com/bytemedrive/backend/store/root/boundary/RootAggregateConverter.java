package com.bytemedrive.backend.store.root.boundary;

import com.bytemedrive.backend.store.root.entity.AbstractAggregate;
import com.bytemedrive.backend.store.root.entity.EventStream;


public interface RootAggregateConverter<T extends AbstractAggregate> {

    /**
     * Converts events stream to aggregate. When aggregate should be removed returns Optional.empty()
     *
     * @param eventStream
     * @return
     */
    T convert(EventStream eventStream);

    Class getAggregateClass();
}
