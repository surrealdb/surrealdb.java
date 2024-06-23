package com.surrealdb;

import java.awt.geom.Point2D;

public class Geometry extends Native {


    Geometry(long ptr) {
        super(ptr);
    }

    private static native boolean isPoint(long ptr);

    private static native double[] getPoint(long ptr);

    final protected native boolean deleteInstance(long ptr);

    public boolean isPoint() {
        return isPoint(getPtr());
    }

    public Point2D.Double getPoint() {
        final double[] coord = getPoint(getPtr());
        return new Point2D.Double(coord[0], coord[1]);
    }
}

