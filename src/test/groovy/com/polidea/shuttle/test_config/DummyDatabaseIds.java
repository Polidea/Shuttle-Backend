package com.polidea.shuttle.test_config;

public class DummyDatabaseIds {

    private int lastId = 1;

    public int next() {
        lastId++;
        return lastId;
    }

}
