package com.bytemedrive.backend.store.root.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jboss.logging.Logger;

import java.util.Objects;
import java.util.StringJoiner;


public abstract class AbstractAggregate {

    private static final Logger log = Logger.getLogger(AbstractAggregate.class);

    public AggregateStatus aggregateStatus = AggregateStatus.ALIVE;

    @JsonIgnore
    public boolean isAlive() {
        return aggregateStatus == AggregateStatus.ALIVE;
    }

    @JsonIgnore
    public boolean isDead() {
        return aggregateStatus == AggregateStatus.DEAD;
    }

    protected boolean isDifferent(String name, Object a, Object b) {
        if (!Objects.equals(a, b)) {
            log.debugf("field: %s is different %s != %s", name, a, b);
            return true;
        }

        return false;
    }

    public abstract String getAggregateId();


    @Override
    public String toString() {
        return new StringJoiner(", ", AbstractAggregate.class.getSimpleName() + "[", "]")
                .add("aggregateStatus=" + aggregateStatus)
                .toString();
    }
}
