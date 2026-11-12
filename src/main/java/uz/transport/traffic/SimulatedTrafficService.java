package uz.transport.traffic;

import com.graphhopper.util.DistanceCalcEarth;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SimulatedTrafficService implements TrafficService {

    private final List<TrafficZone> zones = List.of(
            new TrafficZone("Markaz (Amir Temur)", 41.3111, 69.2797, 100),
            new TrafficZone("Chilonzor", 41.2750, 69.2040, 100));

    @Override
    public double congestionFactor(double lat, double lon) {
        double factor = 1.0;
        for (TrafficZone z : zones) {
            double d = DistanceCalcEarth.DIST_EARTH.calcDist(lat, lon, z.lat(), z.lon());
            if (d < z.radiusMeters()) {
                factor = Math.min(factor, 0.2 + 0.8 * (d / z.radiusMeters()));
            }
        }
        return factor;
    }

    @Override
    public List<TrafficZone> zones() {
        return zones;
    }
}
