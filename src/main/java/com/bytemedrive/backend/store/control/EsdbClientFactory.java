package com.bytemedrive.backend.store.control;

import com.eventstore.dbclient.Endpoint;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.EventStoreDBClientSettings;
import com.eventstore.dbclient.EventStoreDBPersistentSubscriptionsClient;
import io.quarkus.runtime.Startup;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.concurrent.ExecutionException;


@Startup
@ApplicationScoped
public class EsdbClientFactory {

    @ConfigProperty(name = "esdb.hostname")
    String esdbHostname;

    @ConfigProperty(name = "esdb.username")
    String esdbUsername;

    @ConfigProperty(name = "esdb.password")
    String esdbPassword;

    EventStoreDBClient esdbClient;

    EventStoreDBPersistentSubscriptionsClient esdbSubscriptionsClient;

    @PostConstruct
    void startup() {
        var settings = EventStoreDBClientSettings.builder()
                .addHost(new Endpoint(esdbHostname, 2113))
                .tlsVerifyCert(false)
                .tls(true)
                .defaultCredentials(esdbUsername, esdbPassword)
                .buildConnectionSettings();
        esdbClient = EventStoreDBClient.create(settings);
        esdbSubscriptionsClient = EventStoreDBPersistentSubscriptionsClient.create(settings);
    }

    @Produces
    public EventStoreDBClient getEventStoreDBClient() {
        return esdbClient;
    }

    @Produces
    public EventStoreDBPersistentSubscriptionsClient getEventStoreDBPersistentSubscriptionsClient() {
        return esdbSubscriptionsClient;
    }

    @PreDestroy
    void shutdown() throws ExecutionException, InterruptedException {
        if (esdbClient != null) {
            esdbClient.shutdown();
        }
        if (esdbSubscriptionsClient != null) {
            esdbSubscriptionsClient.shutdown();
        }
    }
}
