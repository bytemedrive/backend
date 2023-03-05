package com.bytemedrive.backend.query.customer.boundary;


import com.bytemedrive.backend.query.customer.control.CustomerEntity;
import com.bytemedrive.backend.store.root.boundary.StoreFacade;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;


@ApplicationScoped
public class CustomerFacade {

    @Inject
    EntityManager entityManager;

    @Inject
    StoreFacade storeFacade;

    @Transactional
    public CustomerEntity getCustomer(String customerIdHash) {
        return entityManager.find(CustomerEntity.class, customerIdHash);
    }
}
