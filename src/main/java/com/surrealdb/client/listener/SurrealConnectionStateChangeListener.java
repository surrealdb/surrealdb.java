package com.surrealdb.client.listener;

public interface SurrealConnectionStateChangeListener {

    void onConnectionChange(boolean connected, boolean changedCausedByRemote);

}
