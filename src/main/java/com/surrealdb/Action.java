package com.surrealdb;

/**
 * The action that triggered a live query notification.
 */
public enum Action {
    CREATE(0),
    UPDATE(1),
    DELETE(2);

    private final int code;

    Action(int code) {
        this.code = code;
    }

    static Action fromCode(int code) {
        for (Action action : Action.values()) {
            if (action.code == code) {
                return action;
            }
        }
        throw new SurrealException("Unknown action code: " + code);
    }

    int getCode() {
        return code;
    }
}
