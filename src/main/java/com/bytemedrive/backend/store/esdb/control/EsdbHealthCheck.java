package com.bytemedrive.backend.store.esdb.control;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.ReadStreamOptions;
import com.eventstore.dbclient.RecordedEvent;
import com.eventstore.dbclient.ResolvedEvent;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


@Readiness
@Liveness
@ApplicationScoped
public class EsdbHealthCheck implements HealthCheck {

    private static final String STREAM_NAME = "healthcheck";

    @Inject
    Logger log;

    @Inject
    EventStoreDBClient esdbClient;

    @Override
    public HealthCheckResponse call() {
        String hckMsg = ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
        boolean status = false;
        try {
            var data = EventData.builderAsBinary("health-checked", hckMsg.getBytes(StandardCharsets.UTF_8)).build();
            esdbClient.appendToStream(STREAM_NAME, data).join();

            var readResult = esdbClient.readStream(STREAM_NAME, ReadStreamOptions.get().backwards().maxCount(8).fromEnd()).join();
            var results = readResult.getEvents().stream()
                    .map(ResolvedEvent::getOriginalEvent)
                    .map(RecordedEvent::getEventData)
                    .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                    .toList();

            status = results.contains(hckMsg);
        } catch (Exception e) {
            log.warn("Cannot read health check message: " + hckMsg, e);
        }
        return HealthCheckResponse.named("ESDB connections health check")
                .status(status)
                .withData("msg", hckMsg)
                .build();
    }
}
