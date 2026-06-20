package dev.armand.flappy_wings.util;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

public class SmoothGradient {
    private final TreeMap<Double, Stop> stops = new TreeMap<>();

    private record Stop(double value, Supplier<Boolean> condition) {}

    public void addStop(double x, double value) {
        this.addStop(x, value, () -> true);
    }

    public void addStop(double x, double value, Supplier<Boolean> condition) {
        this.stops.put(x, new Stop(value, condition));
    }

    public double getValue(double x) {
        if (this.stops.isEmpty()) return 0.0;

        Map.Entry<Double, Stop> left = getValidEntry(this.stops.floorEntry(x), true);
        Map.Entry<Double, Stop> right = getValidEntry(this.stops.ceilingEntry(x), false);

        if (left == null && right == null) return 0.0;
        if (left == null) return right.getValue().value();
        if (right == null) return left.getValue().value();

        double x1 = left.getKey();
        double x2 = right.getKey();
        double y1 = left.getValue().value();
        double y2 = right.getValue().value();

        if (x1 == x2) return y1;

        double t = (x - x1) / (x2 - x1);
        double smoothT = t * t * (3.0 - 2.0 * t);

        return y1 + smoothT * (y2 - y1);
    }

    private Map.Entry<Double, Stop> getValidEntry(Map.Entry<Double, Stop> entry, boolean searchDescending) {
        while (entry != null && !entry.getValue().condition().get()) {
            entry = searchDescending ? stops.lowerEntry(entry.getKey()) : stops.higherEntry(entry.getKey());
        }
        return entry;
    }
}