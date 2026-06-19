package dev.armand.monarch_wings.util;

import java.util.Map;
import java.util.TreeMap;

public class SmoothGradient {
    private final TreeMap<Double, Double> stops = new TreeMap<>();

    public void addStop(double x, double value) {
        stops.put(x, value);
    }

    public double getValue(double x) {
        if (stops.isEmpty()) return 0.0;

        Map.Entry<Double, Double> left = stops.floorEntry(x);
        Map.Entry<Double, Double> right = stops.ceilingEntry(x);

        if (left == null) return right.getValue();
        if (right == null) return left.getValue();

        double x1 = left.getKey();
        double x2 = right.getKey();
        double y1 = left.getValue();
        double y2 = right.getValue();

        if (x1 == x2) return y1;

        // 0.0 to 1.0 percentage between stops
        double t = (x - x1) / (x2 - x1);

        // Smoothstep S-curve mapping (Hermite interpolation)
        double smoothT = t * t * (3.0 - 2.0 * t);

        return y1 + smoothT * (y2 - y1);
    }
}
