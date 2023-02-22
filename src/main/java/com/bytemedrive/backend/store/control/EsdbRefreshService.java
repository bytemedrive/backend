package com.bytemedrive.backend.store.control;

import com.bytemedrive.backend.store.entity.AbstractAggregate;
import com.bytemedrive.backend.store.entity.RootAggregate;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.ReadMessage;
import com.eventstore.dbclient.ReadStreamOptions;
import com.eventstore.dbclient.StreamNotFoundException;
import org.jboss.logging.Logger;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;


@ApplicationScoped
public class EsdbRefreshService {

    @Inject
    Logger log;

    @Inject
    EsdbReadService serviceRead;

    @Inject
    EventStoreDBClient esdbClient;

    @Inject
    Event eventBusAggregate;

    public <T extends AbstractAggregate> void refreshAggregates(String aggregateId, Class<T> tClass, boolean aggregateIdInPlainText) {
        log.infof("Refreshing aggregates with class %s, id: %s and aggregateIdInPlainText: %s", tClass.getSimpleName(), aggregateId, aggregateIdInPlainText);

        if (aggregateId != null) {
            refreshSingleAggregate(aggregateId, tClass, aggregateIdInPlainText);
        } else {
            refreshAllAggregates(tClass);
        }
    }

    private <T extends AbstractAggregate> void refreshAllAggregates(Class<T> tClass) {
        var streamName = "$ce-%s".formatted(tClass.getAnnotation(RootAggregate.class).value());

        try {
            esdbClient
                    .readStreamReactive(streamName, ReadStreamOptions.get().forwards().resolveLinkTos())
                    .subscribe(new RefreshSubscriber(tClass));

        } catch (StreamNotFoundException e) {
            log.warnf("Stream %s was not found in ESDB.", streamName);
        }
    }

    <T extends AbstractAggregate> void refreshSingleAggregate(String aggregateId, Class<T> tClass, boolean aggregateIdInPlainText) {
        log.infof("Refreshing single aggregate %s, aggregateId %s", tClass.getSimpleName(), aggregateId);
        Function<Exception, Void> handleException = (Exception exception) -> {
            log.errorf(
                    exception,
                    "Event fire failed - aggregate=%s, aggregateId=%s, aggregateIdInPlainText=%s",
                    tClass,
                    aggregateId,
                    aggregateIdInPlainText
            );

            return null;
        };

        serviceRead.findAggregate(aggregateId, tClass, aggregateIdInPlainText)
                .ifPresent(aggregate ->
                        eventBusAggregate.fireAsync(aggregate).exceptionallyAsync(exception -> handleException.apply((Exception) exception)));
    }


    private final class RefreshSubscriber<T extends AbstractAggregate> implements Subscriber<ReadMessage> {
        private final Class<T> tClass;

        private final Set<String> streamNames;

        private RefreshSubscriber(Class<T> tClass) {
            this.tClass = tClass;
            this.streamNames = new ConcurrentSkipListSet<>();
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(ReadMessage readMessage) {
            if (readMessage != null
                    && readMessage.getEvent() != null
                    && readMessage.getEvent().getEvent() != null
                    && readMessage.getEvent().getEvent().getStreamId() != null) {
                streamNames.add(readMessage.getEvent().getEvent().getStreamId());
            }
        }

        @Override
        public void onError(Throwable t) {
            log.errorf(t, "Error in refresh all subscriber for class: %s", tClass.getSimpleName());
        }

        @Override
        public void onComplete() {
            log.infof("Refresh all subscriber for class %s complete", tClass.getSimpleName());
            streamNames.stream()
                    .map(streamName -> streamName.substring(streamName.indexOf('#') + 1))
                    .forEach(aggregateIdHashed -> refreshSingleAggregate(aggregateIdHashed, tClass, false));
        }
    }

}
