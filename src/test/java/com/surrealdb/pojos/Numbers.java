package com.surrealdb.pojos;

import java.math.BigDecimal;
import java.util.Objects;

public class Numbers {

    public long longPrimitive;
    public Long longObject;
    public int intPrimitive;
    public Integer intObject;
    public short shortPrimitive;
    public Short shortObject;
    public float floatPrimitive;
    public Float floatObject;
    public double doublePrimitive;
    public Double doubleObject;
    public BigDecimal bigDecimal;

    @Override
    public int hashCode() {
        return Objects.hash(longPrimitive, longObject, intPrimitive, intObject, shortPrimitive, shortObject, floatPrimitive, floatObject, doublePrimitive, doubleObject, bigDecimal);
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Numbers numbers = (Numbers) o;
        return longPrimitive == numbers.longPrimitive &&
            intPrimitive == numbers.intPrimitive &&
            shortPrimitive == numbers.shortPrimitive &&
            Float.compare(numbers.floatPrimitive, floatPrimitive) == 0 &&
            Double.compare(numbers.doublePrimitive, doublePrimitive) == 0 &&
            Objects.equals(longObject, numbers.longObject) &&
            Objects.equals(intObject, numbers.intObject) &&
            Objects.equals(shortObject, numbers.shortObject) &&
            Objects.equals(floatObject, numbers.floatObject) &&
            Objects.equals(doubleObject, numbers.doubleObject) &&
            Objects.equals(bigDecimal, numbers.bigDecimal);
    }
}
