package com.surrealdb.java;

import com.surrealdb.java.client.SurrealClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Surreal {

    private final SurrealClient client;

    public Surreal(String host, int port){
        client = new SurrealClient(host, port);
    }

}