package com.surrealdb;

import java.util.UUID;

/**
 * The RecordId class represents a unique identifier for a record in a database.
 * <p>
 * It provides methods to create and manipulate record IDs using either a table
 * name and a long ID or a table name and a string ID. Additionally, it allows
 * retrieval of the table associated with the ID and the ID itself.
 */
public class RecordId extends Native {


    RecordId(long ptr) {
        super(ptr);
    }

    public RecordId(String table, long id) {
        super(newThingLongId(table, id));
    }

    public RecordId(String table, String id) {
        super(newThingStringId(table, id));
    }

    public RecordId(String table, UUID id) {
        super(newThingUuidId(table, id.toString()));
    }

    private static native long newThingLongId(String table, long id);

    private static native long newThingStringId(String table, String id);

    private static native long newThingUuidId(String table, String id);

    private static native String getTable(long ptr);

    private static native long getId(long ptr);

    @Override
    final native String toString(long ptr);

    @Override
    final native int hashCode(long ptr);

    @Override
    final native boolean equals(long ptr1, long ptr2);

    @Override
    final native void deleteInstance(long ptr);


    public String getTable() {
        return getTable(getPtr());
    }

    public Id getId() {
        return new Id(getId(getPtr()));
    }

}

