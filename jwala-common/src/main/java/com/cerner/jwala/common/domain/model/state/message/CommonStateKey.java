package com.cerner.jwala.common.domain.model.state.message;

public enum CommonStateKey implements StateKey {

    AS_OF("AS_OF"),
    ID("ID"),
    STATE("STATE"),
    TYPE("TYPE"),
    PROGRESS("PROGRESS"),
    MESSAGE("MESSAGE");

    private final String key;

    private CommonStateKey(final String theKey) {
        key = theKey;
    }

    @Override
    public String getKey() {
        return key;
    }
}