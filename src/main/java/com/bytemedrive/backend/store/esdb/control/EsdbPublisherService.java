package com.bytemedrive.backend.store.esdb.control;

import com.bytemedrive.backend.privacy.boundary.PrivacyFacade;
import com.bytemedrive.backend.store.root.entity.AggregateId;
import com.bytemedrive.backend.store.root.entity.EventObjectWrapper;
import com.bytemedrive.backend.store.root.entity.Index;
import com.bytemedrive.backend.store.root.entity.IndexType;
import com.bytemedrive.backend.store.root.entity.RootAggregate;
import com.bytemedrive.backend.store.root.entity.StoreEvent;
import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@ApplicationScoped
public class EsdbPublisherService {

    @Inject
    Logger log;

    @Inject
    EventStoreDBClient esdbClient;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    PrivacyFacade privacyFacade;

    public CompletableFuture<Void> publishEvent(Object event) {
        return CompletableFuture.runAsync(() -> {
            try {
                var oAggregateStreamName = writeData(event);
                if (oAggregateStreamName.isPresent()) {
                    writeIndices(event, oAggregateStreamName.get());
                }
            } catch (Exception e) {
                log.error("Cannot publish event " + event, e);
            }
        });
    }

    private Optional<String> writeData(Object event) throws InvocationTargetException, IllegalAccessException, JsonProcessingException {
        log.debugf("write data event: %s", event);
        StoreEvent storeEvent = event.getClass().getAnnotation(StoreEvent.class);
        var rootAggregateName = storeEvent.aggregate().getAnnotation(RootAggregate.class).value();
        var aggregateId = getAggregateId(event);
        log.infof("ESDB write event %s for aggregate: %s with aggregateId: %s", storeEvent.name(), rootAggregateName, aggregateId);
        if (aggregateId == null) {
            log.warnf("Cannot publish event with null aggregateId. Event %s", event);
            return Optional.empty();
        }
        var eventName = storeEvent.name();
        log.debugf("write data eventName: %s", eventName);
        var aggregateIdSha3 = privacyFacade.hashSha3(aggregateId);
        log.debugf("write data aggregateIdSha3: %s", aggregateIdSha3);
        var eventBytes = objectMapper.writeValueAsBytes(new EventObjectWrapper(eventName, aggregateId, ZonedDateTime.now(), event));
        var data = EventData.builderAsBinary(eventName, eventBytes).build();
        var aggregateStreamName = "%s#%s".formatted(rootAggregateName, aggregateIdSha3);
        log.infof("write data aggregateStreamName: %s", aggregateStreamName);
        esdbClient.appendToStream(aggregateStreamName, data);
        return Optional.ofNullable(aggregateStreamName);
    }

    private void writeIndices(Object event, String aggregateIdSha3) {
        var indices = Arrays.stream(event.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Index.class))
                .map(this::setAccessible)
                .filter(field -> fieldValue(field, event) != null)
                .collect(Collectors.toMap(
                        field -> field.getAnnotation(Index.class).value(),
                        field -> String.valueOf(fieldValue(field, event))));

        for (Map.Entry<IndexType, String> entry : indices.entrySet()) {
            var indexType = entry.getKey();
            log.debugf("write indices indexType: %s", indexType);
            var indexValue = entry.getValue();
            log.debugf("write indices indexValue: %s", indexValue);
            var indexValueSha3 = privacyFacade.hashSha3(indexValue);
            var indexStreamName = "%s#%s".formatted(indexType.getName(), indexValueSha3);
            log.debugf("write indices indexStreamName: %s", indexStreamName);
            var data = EventData
                    .builderAsBinary(indexType.getName(), aggregateIdSha3.getBytes(StandardCharsets.UTF_8))
                    .build();

            esdbClient.appendToStream(indexStreamName, data);
        }
    }

    private String getAggregateId(Object event) throws InvocationTargetException, IllegalAccessException {
        var oField = Arrays.stream(event.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(AggregateId.class)).findAny();

        if (oField.isEmpty()) {
            throw new IllegalArgumentException("StoreEvent does not have any field marked by AggregateId annotation to find id" + event.getClass());
        }
        var oMethod = Arrays.stream(event.getClass().getDeclaredMethods()).filter(method -> method.getName().equals(oField.get().getName())).findAny();
        if (oMethod.isEmpty()) {
            throw new IllegalArgumentException("StoreEvent does not have any method named as field marked by AggregateId annotation to find id" + event.getClass());
        }

        return String.class.cast(oMethod.get().invoke(event));
    }

    private Object fieldValue(Field field, Object event) {
        try {
            return field.get(event);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot get field value from object=%s".formatted(event));
        }
    }

    private Field setAccessible(Field field) {
        field.setAccessible(true);
        return field;
    }

}
