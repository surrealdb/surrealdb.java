package com.surrealdb.refactor.types.surrealdb;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.surrealdb.refactor.exception.SurrealDBUnimplementedException;
import com.surrealdb.refactor.types.IntoJson;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Value implements IntoJson {

    private final String string;
    private final Number number;
    private final ObjectValue object;

    /**
     * Create a Value that represents a Strand type
     *
     * @param string the Strand to represent in this Value
     */
    public Value(final String string) {
        this.string = string;
        this.number = null;
        this.object = null;
    }

    /**
     * Create a Value that represents a SurrealDB number
     *
     * @param number the number to represent
     */
    public Value(final Number number) {
        this.string = null;
        this.number = number;
        this.object = null;
    }

    public Value(final ObjectValue object) {
        this.string = null;
        this.number = null;
        this.object = object;
    }

    public static Value fromJson(final JsonElement json) {
        if (json.isJsonPrimitive()) {
            final JsonPrimitive primitive = json.getAsJsonPrimitive();
            if (primitive.isNumber()) {
                return new Value(new Number(primitive.getAsNumber().floatValue()));
            }
            if (primitive.isString()) {
                return new Value(primitive.getAsString());
            }
        }
        throw new SurrealDBUnimplementedException(
                "https://github.com/surrealdb/surrealdb.java/issues/63",
                "Parsing general values from JSON is not implemented yet.");
    }

    public boolean isString() {
        return this.string != null;
    }

    public Optional<String> asString() {
        return Optional.ofNullable(this.string);
    }

    public boolean isNumber() {
        return this.number != null;
    }

    public Optional<Number> asNumber() {
        return Optional.ofNullable(this.number);
    }

    @Override
    public JsonElement intoJson() {
        if (this.isNumber()) {
            return new JsonPrimitive(this.asNumber().get().asFloat().get());
        }
        if (this.isString()) {
            return new JsonPrimitive(this.asString().get());
        }
        System.out.printf("This is value causing failure: %s\n", this);
        throw new SurrealDBUnimplementedException(
                "https://github.com/surrealdb/surrealdb.java/issues/63",
                "Parsing general values from JSON is not implemented yet.");
    }
}
