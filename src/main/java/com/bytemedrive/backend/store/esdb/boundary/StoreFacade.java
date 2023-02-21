package com.bytemedrive.backend.store.esdb.boundary;

import com.bytemedrive.backend.store.esdb.control.EsdbPublisherService;
import com.bytemedrive.backend.store.esdb.control.EsdbReadService;
import com.bytemedrive.backend.store.esdb.control.EsdbRefreshService;
import com.bytemedrive.backend.store.root.entity.AbstractAggregate;
import com.bytemedrive.backend.store.root.entity.EventMapWrapper;
import com.bytemedrive.backend.store.root.entity.IndexType;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@ApplicationScoped
public class StoreFacade {

    @Inject
    Logger log;

    @Inject
    EsdbPublisherService servicePublisher;

    @Inject
    EsdbReadService serviceRead;

    @Inject
    EsdbRefreshService serviceRefresh;

    /**
     * Saves given event into an event store.
     *
     * @param event object of an event should StoreEvent annotation and
     * an attribute with AggregateId annotation
     * @return
     */
    public CompletableFuture<Void> publishEvent(Object event) {
        return servicePublisher.publishEvent(event);
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
        return CompletableFuture.supplyAsync(() -> serviceRead.findAggregate(aggregateId, tClass, aggregateIdInPlainText));
    }

    public <T extends AbstractAggregate> Optional<T> findAggregateByIndex(String indexValue, IndexType indexName, Class<T> tClass) {
        return serviceRead.findAggregateByIndex(indexValue, indexName, tClass);
    }

    public <T extends AbstractAggregate> CompletableFuture<List<T>> getAggregates(Class<T> tClass) {
        return CompletableFuture.supplyAsync(() -> serviceRead.getAggregates(tClass))
                .exceptionally(throwable -> {
                    log.warnf("Loading all aggregates of %s failed with error: %s", tClass.getName(), throwable.getMessage());
                    return Collections.emptyList();
                });
    }

    public <T extends AbstractAggregate> CompletableFuture<List<EventMapWrapper>> getEvents(String aggregateId, Class<T> tClass, boolean aggregateIdInPlainText) {
        return CompletableFuture.supplyAsync(() -> serviceRead.getWrapperEvents(aggregateId, tClass, aggregateIdInPlainText))
                .exceptionally(throwable -> {
                    log.warnf("Loading all events of %s with aggregateId %s  and aggregateIdInPlainText %s failed with error: %s",
                            tClass.getName(), aggregateId, aggregateIdInPlainText, throwable.getMessage());
                    return Collections.emptyList();
                });
    }

    public <T extends AbstractAggregate> CompletableFuture<Void> refreshAggregates(String aggregateId, Class<T> tClass, boolean aggregateIdInPlainText) {
        return CompletableFuture.runAsync(() -> serviceRefresh.refreshAggregates(aggregateId, tClass, aggregateIdInPlainText))
                .exceptionally(throwable -> {
                    log.warnf("Error in refreshing aggregates %s, aggregateId: %s, aggregateIdInPlainText: %s", tClass, aggregateId, aggregateIdInPlainText);
                    return null;
                });
    }
}
