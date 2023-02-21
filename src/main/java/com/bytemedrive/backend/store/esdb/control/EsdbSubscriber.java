package com.bytemedrive.backend.store.esdb.control;

import com.eventstore.dbclient.EventStoreDBPersistentSubscriptionsClient;
import com.eventstore.dbclient.PersistentSubscription;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;


@Startup
@ApplicationScoped
public class EsdbSubscriber {

    @Inject
    Logger log;

    @ConfigProperty(name = "esdb.subscription-group-name")
    String subscriptionName;

    @Inject
    EventStoreDBPersistentSubscriptionsClient esdbSubscriptionsClient;

    @Inject
    EsdbListener esdbListener;

    CompletableFuture<PersistentSubscription> futureSubscription;

    void startup(@Observes StartupEvent event) {
        futureSubscription = esdbSubscriptionsClient.subscribeToAll(subscriptionName, esdbListener)
                .exceptionally(throwable -> {
                    log.warnf(throwable, "Subscribe to all failed");
                    return null;
                });
    }

    void shutdown(@Observes ShutdownEvent event) {
        if (futureSubscription != null && futureSubscription.isDone()) {
            futureSubscription.join().stop();
        }
    }
}
