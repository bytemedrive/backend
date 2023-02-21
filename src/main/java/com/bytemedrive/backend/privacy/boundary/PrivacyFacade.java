package com.bytemedrive.backend.privacy.boundary;


import com.bytemedrive.backend.privacy.control.ShaService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


@ApplicationScoped
public class PrivacyFacade {

    @Inject
    ShaService shaService;

    public String hashSha3(String input) {
        return shaService.hashSha3(input);
    }
}
