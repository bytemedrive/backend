package com.bytemedrive.backend.query.customer.boundary;


import com.bytemedrive.backend.query.customer.control.CustomerEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;


@ApplicationScoped
public class CustomerFacade {

    @Inject
    EntityManager entityManager;

    @Transactional
    public CustomerEntity getCustomer(String customerIdHash) {
        return entityManager.createQuery("select c from CustomerEntity c where c.id = :id", CustomerEntity.class)
                .setParameter("id", customerIdHash)
                .getSingleResult();
    }
}
