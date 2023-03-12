package com.bytemedrive.backend.query.customer.control;


import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;


@Entity
@Table(name = "customer")
@TypeDef(name = "json", typeClass = JsonType.class)
public class CustomerEntity {

    @Id
    public String id;

    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    public List<String> events;

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        CustomerEntity that = (CustomerEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(events, that.events);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, events);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CustomerEntity.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("events=" + events)
                .toString();
    }
}
