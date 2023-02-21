package com.bytemedrive.backend.query.customer.control;


import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;


@Entity
@Table(name = "customer")
public class CustomerEntity {

    @Id
    public String id;

    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    public List<String> events;
}
