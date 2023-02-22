package com.bytemedrive.backend.store.control;

import com.bytemedrive.backend.store.entity.AbstractAggregate;
import com.bytemedrive.backend.store.entity.EventStream;


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
