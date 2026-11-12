
package uz.transport.monitoring.trip;

import java.util.List;

final class GeoUtil {

    private static final double METERS_PER_DEGREE = 111_320.0;

    private GeoUtil() {
    }

    /** Nuqtadan marshrut chizig'igacha eng qisqa masofa (metr). Marshrut nuqtalari: [lat, lon]. */
    static double distanceToPolylineMeters(double lat, double lon, List<double[]> pts) {
        if (pts.isEmpty()) {
            return Double.MAX_VALUE;
        }
        double cos = Math.cos(Math.toRadians(lat));
        double best = Double.MAX_VALUE;
        for (int i = 0; i < pts.size() - 1; i++) {
            double ax = (pts.get(i)[1] - lon) * cos * METERS_PER_DEGREE;
            double ay = (pts.get(i)[0] - lat) * METERS_PER_DEGREE;
            double bx = (pts.get(i + 1)[1] - lon) * cos * METERS_PER_DEGREE;
            double by = (pts.get(i + 1)[0] - lat) * METERS_PER_DEGREE;
            best = Math.min(best, pointToSegment(ax, ay, bx, by));
        }
        if (pts.size() == 1) {
            best = Math.hypot((pts.get(0)[1] - lon) * cos * METERS_PER_DEGREE,
                    (pts.get(0)[0] - lat) * METERS_PER_DEGREE);
        }
        return best;
    }

    /** Nuqta koordinata boshida (0,0), kesma (ax,ay)-(bx,by). */
    private static double pointToSegment(double ax, double ay, double bx, double by) {
        double dx = bx - ax, dy = by - ay;
        double len2 = dx * dx + dy * dy;
        double t = len2 == 0 ? 0 : Math.max(0, Math.min(1, -(ax * dx + ay * dy) / len2));
        return Math.hypot(ax + t * dx, ay + t * dy);
    }
}