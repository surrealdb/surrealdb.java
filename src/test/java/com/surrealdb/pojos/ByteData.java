package com.surrealdb.pojos;

import java.util.Arrays;
import java.util.Objects;

public class ByteData {

    public byte[] data;

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(data));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ByteData byteData = (ByteData) o;
        return Arrays.equals(data, byteData.data);
    }
}

