package com.bytemedrive.backend.store.root.boundary;

import com.bytemedrive.backend.store.root.entity.AbstractAggregate;
import com.bytemedrive.backend.store.root.entity.EventMapWrapper;
import com.bytemedrive.backend.store.root.entity.IndexType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@ApplicationScoped
public class StoreFacade {

    @Inject
    com.bytemedrive.backend.store.esdb.boundary.StoreFacade facadeEsdb;

    /**
     * Saves given event into an event store.
     *
     * @param event object of an event should StoreEvent annotation and
     * an attribute with AggregateId annotation
     * @return
     */
    public CompletableFuture<Void> publishEvent(Object event) {
        return facadeEsdb.publishEvent(event);
    }

    /**
     * Loads all events of aggregate and converts them to Aggregate. Should not be used in blocking thread.
     *
     * @param aggregateId
     * @param tClass
     * @param <T>
     * @return
     */
    public <T extends AbstractAggregate> CompletableFuture<Optional<T>> findAggregate(String aggregateId, Class<T> tClass) {
        return findAggregate(aggregateId, tClass, true);
    }

    public <T extends AbstractAggregate> CompletableFuture<Optional<T>> findAggregate(String aggregateId, Class<T> tClass, boolean aggregateIdInPlainText) {
        return facadeEsdb.findAggregate(aggregateId, tClass, aggregateIdInPlainText);
    }

    public <T extends AbstractAggregate> CompletableFuture<List<T>> getAggregates(Class<T> tClass) {
        return facadeEsdb.getAggregates(tClass);
    }

    public <T extends AbstractAggregate> Optional<T> findAggregateByIndex(String indexValue, IndexType indexName, Class<T> tClass) {
        return facadeEsdb.findAggregateByIndex(indexValue, indexName, tClass);
    }

    public <T extends AbstractAggregate> CompletableFuture<List<EventMapWrapper>> getEvents(String aggregateId, Class<T> tClass, boolean aggregateIdInPlainText) {
        return facadeEsdb.getEvents(aggregateId, tClass, aggregateIdInPlainText);
    }
}
