package com.bytemedrive.backend.store.entity;


import com.fasterxml.jackson.annotation.JsonValue;


public enum IndexType {

    REFERRAL_CODE("ix_referral_code"),

    FRIENDLY_REFERRAL_CODE("ix_friendly_referral_code"),

    NFT_TOKEN_ID("ix_nft_token_id"),

    WALLET_ADDRESS("ix_wallet_address"),

    LAND_REGISTRATION_ID("ix_land_registration");

    private final String name;

    IndexType(String name) {
        this.name = name;
    }

    public static IndexType of(String name) {
        for (var value : IndexType.values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }

        throw new IllegalArgumentException("There is no IndexType with name: " + name);
    }

    @JsonValue
    public String getName() {
        return name;
    }
}
