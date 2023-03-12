package com.bytemedrive.backend.store.esdb.control;

import com.bytemedrive.backend.privacy.boundary.PrivacyFacade;
import com.bytemedrive.backend.store.root.boundary.RootAggregateConverter;
import com.bytemedrive.backend.store.root.entity.AbstractAggregate;
import com.bytemedrive.backend.store.root.entity.EventMapWrapper;
import com.bytemedrive.backend.store.root.entity.EventStream;
import com.bytemedrive.backend.store.root.entity.IndexType;
import com.bytemedrive.backend.store.root.entity.RootAggregate;
import com.bytemedrive.backend.store.root.entity.StoreEvent;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.ReadStreamOptions;
import com.eventstore.dbclient.RecordedEvent;
import com.eventstore.dbclient.ResolvedEvent;
import com.eventstore.dbclient.StreamNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;
import org.reflections.Reflections;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@ApplicationScoped
public class EsdbReadService {

    @Inject
    Logger log;

    @Inject
    EventStoreDBClient esdbClient;

    @Inject
    PrivacyFacade facadePrivacy;

    Map<String, Class<?>> eventMap;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    Instance<RootAggregateConverter<?>> converter;

    @PostConstruct
    void init() {
        eventMap = new Reflections("com.bytemedrive.backend")
                .getTypesAnnotatedWith(StoreEvent.class)
                .stream()
                .collect(Collectors.toMap(
                        type -> type.getAnnotation(StoreEvent.class).name(),
                        type -> type
                ));
    }

    public <T extends AbstractAggregate> List<EventMapWrapper> getWrapperEvents(String aggregateId, Class<T> tClass, boolean aggregateIdInPlainText) {
        return getAllMessages(aggregateId, tClass, aggregateIdInPlainText).stream()
                .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                .flatMap(this::toEventMapWrapper)
                .toList();
    }

    private <T extends AbstractAggregate> List<byte[]> getAllMessages(String aggregateId, Class<T> tClass, boolean aggregateIdInPlainText) {
        var aggregateIdSha3 = aggregateIdInPlainText ? facadePrivacy.hashSha3(aggregateId) : aggregateId;
        var streamName = "%s#%s".formatted(
                tClass.getAnnotation(RootAggregate.class).value(),
                aggregateIdSha3
        );

        try {
            var readResult = esdbClient
                    .readStream(streamName, ReadStreamOptions.get().forwards())
                    .join();
            return readResult.getEvents().stream()
                    .map(ResolvedEvent::getOriginalEvent)
                    .map(RecordedEvent::getEventData)
                    .toList();

        } catch (StreamNotFoundException e) {
            log.warnf("stream %s was not found", streamName);
        }
        return Collections.emptyList();
    }

    public <T extends AbstractAggregate> Optional<T> findAggregate(String aggregateId, Class<T> tClass, boolean aggregateIdInPlainText) {
        log.debugf("Finding aggregate with class %s and id[plainText: %s]: %s", tClass.getSimpleName(), aggregateIdInPlainText, aggregateId);
        var wrapperEvents = getWrapperEvents(aggregateId, tClass, aggregateIdInPlainText);
        return convertWrappedEventsToAggregate(wrapperEvents, tClass, aggregateId);
    }

    private <T extends AbstractAggregate> Optional<T> convertWrappedEventsToAggregate(List<EventMapWrapper> wrapperEvents, Class<T> tClass, String aggregateId) {
        var events = wrapperEvents.stream()
                .flatMap(this::convertToAppEvent)
                .toList();
        if (events == null || events.isEmpty()) {
            return Optional.empty();
        }
        var eventStream = new EventStream(aggregateId, events);
        var aggregate = converter.stream()
                .filter(c -> c.getAggregateClass().equals(tClass))
                .flatMap(c -> {
                    try {
                        return Stream.of(c.convert(eventStream));
                    } catch (Exception e) {
                        log.errorf(e, "Error in converting event stream to aggregate class %s: %s",
                                tClass.getSimpleName(), aggregateId);
                        return Stream.empty();
                    }
                })
                .findAny();
        if (aggregate.isPresent()) {
            log.debugf("Found aggregate %s, aggregate id: %s", aggregate, aggregateId);
            return Optional.of((T) aggregate.get());
        }

        log.debugf("Aggregate %s, aggregate id: %s was not found", aggregate, aggregateId);
        return Optional.empty();
    }

    public <T extends AbstractAggregate> Optional<T> findAggregateByIndex(String indexValue, IndexType indexType, Class<T> tClass) {
        var indexStreamName = "%s#%s".formatted(indexType.getName(), facadePrivacy.hashSha3(indexValue));

        try {
            var readResult = esdbClient
                    .readStream(indexStreamName, ReadStreamOptions.get().backwards().maxCount(1))
                    .join();
            var results = readResult.getEvents().stream()
                    .map(ResolvedEvent::getOriginalEvent)
                    .map(RecordedEvent::getEventData)
                    .toList();
            if (results == null || results.isEmpty()) {
                return Optional.empty();
            }

            var aggregateStreamName = new String(results.get(0), StandardCharsets.UTF_8);
            var aggregateIdSha3 = aggregateStreamName.substring(aggregateStreamName.indexOf('#') + 1);
            return findAggregate(aggregateIdSha3, tClass, false);
        } catch (StreamNotFoundException e) {
            log.warnf("index stream %s was not found", indexStreamName);
        }

        return Optional.empty();
    }

    private Stream<EventMapWrapper> toEventMapWrapper(String eventJson) {
        try {
            return Stream.of(objectMapper.readValue(eventJson, EventMapWrapper.class));
        } catch (JsonProcessingException e) {
            log.debugf(e, "JSON cannot be created from eventJson: %s", eventJson);
        } catch (Exception e) {
            log.warnf(e, "Cannot crate a JSON from eventJson: %s", eventJson);
        }
        return Stream.empty();
    }

    private Stream<?> convertToAppEvent(EventMapWrapper eventMapWrapper) {
        log.debugf("Converting general event %s", eventMapWrapper);
        if (eventMap.containsKey(eventMapWrapper.eventName())) {
            try {
                String json = objectMapper.writeValueAsString(eventMapWrapper.data());
                return Stream.of(objectMapper.readValue(json, eventMap.get(eventMapWrapper.eventName())));
            } catch (JsonProcessingException e) {
                log.debug("Cannot convert general event " + eventMapWrapper, e);
            } catch (Exception e) {
                log.warn("General event convert failed " + eventMapWrapper, e);
            }
        }
        return Stream.empty();
    }

    public <T extends AbstractAggregate> List<T> getAggregates(Class<T> tClass) {
        log.debugf("Finding aggregates with class %s", tClass.getSimpleName());

        var streamName = "$ce-%s".formatted(tClass.getAnnotation(RootAggregate.class).value());

        try {
            var readResult = esdbClient
                    .readStream(streamName, ReadStreamOptions.get().resolveLinkTos().forwards())
                    .join();
            var recordedEvents = readResult.getEvents().stream()
                    .map(ResolvedEvent::getEvent)
                    .toList();

            var map = new HashMap<String, List<byte[]>>();
            for (var re : recordedEvents) {
                var aggregatedIdHashed = re.getStreamId().substring(re.getStreamId().indexOf('#') + 1);
                if (!map.containsKey(aggregatedIdHashed)) {
                    map.put(aggregatedIdHashed, new ArrayList<>());
                }
                map.get(aggregatedIdHashed).add(re.getEventData());
            }

            var results = new ArrayList<T>();
            for (var entry : map.entrySet()) {
                var wrapperEvents = entry.getValue().stream()
                        .map(eventByte -> new String(eventByte, StandardCharsets.UTF_8))
                        .flatMap(this::toEventMapWrapper).toList();

                convertWrappedEventsToAggregate(wrapperEvents, tClass, entry.getKey())
                        .ifPresent(results::add);
            }

            return Collections.unmodifiableList(results);
        } catch (StreamNotFoundException e) {
            log.warnf("Stream %s was not found in ESDB.", streamName);
        }
        return Collections.emptyList();
    }
}
