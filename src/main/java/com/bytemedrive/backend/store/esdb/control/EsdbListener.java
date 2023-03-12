package com.bytemedrive.backend.store.esdb.control;

import com.bytemedrive.backend.store.root.entity.StoreEvent;
import com.eventstore.dbclient.PersistentSubscription;
import com.eventstore.dbclient.PersistentSubscriptionListener;
import com.eventstore.dbclient.ResolvedEvent;
import org.jboss.logging.Logger;
import org.reflections.Reflections;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
import java.util.stream.Collectors;


@ApplicationScoped
public class EsdbListener extends PersistentSubscriptionListener {

    @Inject
    Logger log;

    @Inject
    EsdbRefreshService serviceRefresh;

    // event name => aggregate class
    Map<String, Class> mapEvents;

    @PostConstruct
    void init() {
        mapEvents = new Reflections("com.bytemedrive.backend")
                .getTypesAnnotatedWith(StoreEvent.class)
                .stream()
                .map(est -> est.getAnnotation(StoreEvent.class))
                .collect(Collectors.toMap(
                        se -> se.name(),
                        se -> se.aggregate()
                ));
    }

    @Override
    public void onEvent(PersistentSubscription subscription, int retryCount, ResolvedEvent event) {
        log.infof("Event from stream name %s and type %s", event.getEvent().getStreamId(), event.getEvent().getEventType());
        subscription.ack(event);

        var eventName = event.getEvent().getEventType();
        if (mapEvents.containsKey(eventName)) {
            var aggregateId = event.getEvent().getStreamId().substring(event.getEvent().getStreamId().indexOf('#') + 1);
            var aggregateClass = mapEvents.get(eventName);
            serviceRefresh.refreshSingleAggregate(aggregateId, aggregateClass, false);
        }
    }
}
