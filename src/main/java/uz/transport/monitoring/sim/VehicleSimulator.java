package uz.transport.monitoring.sim;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.PointList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.transport.traffic.TrafficService;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;


@Component
public class VehicleSimulator {

    private static final Logger log = LoggerFactory.getLogger(VehicleSimulator.class);

    private static final double MIN_LAT = 41.24, MAX_LAT = 41.37;
    private static final double MIN_LON = 69.18, MAX_LON = 69.34;
    private static final int VEHICLE_COUNT = 12;
    private static final double SIM_SPEEDUP = 3.0;

    private final GraphHopper hopper;
    private final TrafficService traffic;
    private final List<SimVehicle> vehicles = new CopyOnWriteArrayList<>();
    private final Random rnd = new Random();

    public VehicleSimulator(GraphHopper hopper, TrafficService traffic) {
        this.hopper = hopper;
        this.traffic = traffic;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        for (int i = 1; i <= VEHICLE_COUNT; i++) {
            SimVehicle v = new SimVehicle(i, "Mashina " + i, 35 + rnd.nextInt(26));
            if (assignRoute(v)) {
                vehicles.add(v);
            }
        }
        log.info("Simulyatsiya: {} ta mashina ishga tushdi", vehicles.size());
    }

    @Scheduled(fixedRate = 1000)
    public void tick() {
        for (SimVehicle v : vehicles) {
            double target = v.freeSpeedKmh * traffic.congestionFactor(v.lat, v.lon);
            v.speedKmh += (target - v.speedKmh) * 0.3;
            v.distance += (v.speedKmh / 3.6) * SIM_SPEEDUP;
            if (v.distance >= v.total) {
                assignRoute(v);
            } else {
                place(v);
            }
        }
    }

    public List<VehicleDto> snapshot() {
        return vehicles.stream().map(v -> {
            double ratio = v.speedKmh / v.freeSpeedKmh;
            String status = ratio > 0.7 ? "green" : ratio > 0.4 ? "yellow" : "red";
            return new VehicleDto(v.id, v.name, v.lat, v.lon, (int) Math.round(v.speedKmh), status);
        }).toList();
    }

    private boolean assignRoute(SimVehicle v) {
        for (int attempt = 0; attempt < 40; attempt++) {
            double aLat, aLon;
            if (v.hasPosition) {
                aLat = v.lat;
                aLon = v.lon;
            } else {
                aLat = rand(MIN_LAT, MAX_LAT);
                aLon = rand(MIN_LON, MAX_LON);
            }
            double bLat = rand(MIN_LAT, MAX_LAT);
            double bLon = rand(MIN_LON, MAX_LON);
            if (DistanceCalcEarth.DIST_EARTH.calcDist(aLat, aLon, bLat, bLon) < 2500) {
                continue;
            }
            GHResponse rsp = hopper.route(new GHRequest(aLat, aLon, bLat, bLon).setProfile("car"));
            if (rsp.hasErrors()) {
                continue;
            }
            PointList pts = rsp.getBest().getPoints();
            if (pts.size() < 2) {
                continue;
            }
            v.setPath(pts);
            return true;
        }
        return false;
    }

    private void place(SimVehicle v) {
        while (v.seg < v.cum.length - 2 && v.cum[v.seg + 1] < v.distance) {
            v.seg++;
        }
        double segLen = v.cum[v.seg + 1] - v.cum[v.seg];
        double t = segLen <= 0 ? 0 : (v.distance - v.cum[v.seg]) / segLen;
        t = Math.max(0, Math.min(1, t));
        double lat1 = v.path.getLat(v.seg), lon1 = v.path.getLon(v.seg);
        double lat2 = v.path.getLat(v.seg + 1), lon2 = v.path.getLon(v.seg + 1);
        v.lat = lat1 + (lat2 - lat1) * t;
        v.lon = lon1 + (lon2 - lon1) * t;
    }

    private double rand(double min, double max) {
        return min + rnd.nextDouble() * (max - min);
    }
}
