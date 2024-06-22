package com.surrealdb;

import java.awt.geom.Point2D;

public class Geometry implements AutoCloseable {

    private long id;

    Geometry(long id) {
        this.id = id;
    }

    private static native boolean deleteInstance(long id);

    private static native boolean isPoint(long id);

    private static native double[] getPoint(long id);

    @Override
    public void close() {
        deleteInstance(id);
        id = 0;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    public boolean isPoint() {
        return isPoint(id);
    }

    public Point2D.Double getPoint() {
        final double[] coord = getPoint(id);
        return new Point2D.Double(coord[0], coord[1]);
    }
}

