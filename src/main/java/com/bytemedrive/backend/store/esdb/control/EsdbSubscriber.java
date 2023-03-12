package com.bytemedrive.backend.store.esdb.control;

import com.eventstore.dbclient.CreatePersistentSubscriptionToAllOptions;
import com.eventstore.dbclient.EventStoreDBPersistentSubscriptionsClient;
import com.eventstore.dbclient.NamedConsumerStrategy;
import com.eventstore.dbclient.PersistentSubscription;
import com.eventstore.dbclient.SubscriptionFilter;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Optional;
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

    Optional<PersistentSubscription> oSubscription = Optional.empty();

    void startup(@Observes StartupEvent event) {
        CompletableFuture.runAsync(() -> {
                if (!persistentSubscriptionExists()) {
                    createPersistentSubscription();
                }
                subscribeForEvents();
        }).exceptionally(throwable -> {
            log.errorf(throwable, "Error in subscribing for events with name: %s", subscriptionName);
            return null;
        });
    }

    private boolean persistentSubscriptionExists() {
        var oPersistentSubscription = esdbSubscriptionsClient.getInfoToAll(subscriptionName).join();
        if (oPersistentSubscription.isPresent()) {
            var persistentSubscription = oPersistentSubscription.get();
            if ("Live".equals(persistentSubscription.getStatus()) && "$all".equals(persistentSubscription.getEventSource())) {
                log.infof("ESDB persistent subscription for name: %s does exist.", subscriptionName);
                return true;
            }
        }
        log.infof("ESDB persistent subscription for name: %s does NOT exist.", subscriptionName);
        return false;
    }

    private void createPersistentSubscription() {
        log.infof("ESDB creating persistent subscription for name: %s.", subscriptionName);
        SubscriptionFilter filter = SubscriptionFilter.newBuilder()
                .withStreamNameRegularExpression("customer#.*|certificate#.*")
                .build();

        esdbSubscriptionsClient.createToAll(
                        subscriptionName,
                        CreatePersistentSubscriptionToAllOptions.get()
                                .filter(filter)
                                .namedConsumerStrategy(NamedConsumerStrategy.PINNED)
                                .fromEnd())
                .exceptionally(throwable -> {
                    log.errorf((Throwable) throwable, "Error when creating persistent subscription to $all with name: %s", subscriptionName);
                    return null;
                }).join();
    }

    private void subscribeForEvents() {
        log.infof("ESDB subscribing for events with name: %s.", subscriptionName);
        oSubscription = Optional.ofNullable(
                esdbSubscriptionsClient.subscribeToAll(subscriptionName, esdbListener)
                        .exceptionally(throwable -> {
                            log.warnf(throwable, "Subscribe to all failed");
                            return null;
                        })
                        .join());
    }

    void shutdown(@Observes ShutdownEvent event) {
        oSubscription.ifPresent(subscription -> {
            subscription.stop();
            log.infof("ESDB shutting down events consuming with name: %s.", subscriptionName);
        });
    }
}
