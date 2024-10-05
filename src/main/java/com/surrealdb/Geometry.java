package com.surrealdb;

import java.awt.geom.Point2D;

public class Geometry extends Native {


    Geometry(long ptr) {
        super(ptr);
    }

    private static native boolean isPoint(long ptr);

    private static native double[] getPoint(long ptr);

    @Override
    final native String toString(long ptr);

    @Override
    final native int hashCode(long ptr);

    @Override
    final native boolean equals(long ptr1, long ptr2);

    @Override
    final native boolean deleteInstance(long ptr);

    public boolean isPoint() {
        return isPoint(getPtr());
    }

    public Point2D.Double getPoint() {
        final double[] coord = getPoint(getPtr());
        return new Point2D.Double(coord[0], coord[1]);
    }
}

