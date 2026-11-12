package uz.transport.monitoring;



import com.graphhopper.util.DistanceCalcEarth;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.transport.monitoring.domain.Alert;
import uz.transport.monitoring.domain.GpsPoint;
import uz.transport.monitoring.domain.Vehicle;
import uz.transport.monitoring.repo.AlertRepository;
import uz.transport.monitoring.repo.GpsPointRepository;
import uz.transport.monitoring.repo.VehicleRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class MonitoringService {

    private static final long OFFLINE_AFTER_SECONDS = 30;
    private static final double STOPPED_BELOW_KMH = 3.0;
    private static final String SPEEDING = "SPEEDING";

    private final VehicleRepository vehicles;
    private final GpsPointRepository points;
    private final AlertRepository alerts;
    private final GeofenceService geofences;
    private final StopDetector stops;

    public MonitoringService(VehicleRepository vehicles, GpsPointRepository points, AlertRepository alerts,
                             GeofenceService geofences, StopDetector stops) {
        this.vehicles = vehicles;
        this.points = points;
        this.alerts = alerts;
        this.geofences = geofences;
        this.stops = stops;
    }

    @Transactional
    public Vehicle ensureVehicle(String name, int speedLimitKmh) {
        return vehicles.findByName(name)
                .orElseGet(() -> vehicles.save(new Vehicle(name, "", speedLimitKmh)));
    }

    @Transactional
    public Vehicle createVehicle(String name, String plate, int speedLimitKmh) {
        return vehicles.save(new Vehicle(name, plate, speedLimitKmh));
    }

    @Transactional(readOnly = true)
    public List<Vehicle> allVehicles() {
        return vehicles.findAll();
    }

    @Transactional
    public GpsPoint recordPoint(Long vehicleId, double lat, double lon, double speedKmh, Instant time) {
        Vehicle v = vehicles.findById(vehicleId)
                .orElseThrow(() -> new NoSuchElementException("Mashina topilmadi: " + vehicleId));
        GpsPoint p = points.save(new GpsPoint(vehicleId, lat, lon, speedKmh, time));

        if (speedKmh > v.getSpeedLimitKmh()) {
            Instant since = time.minusSeconds(60);
            boolean recent = alerts.findFirstByVehicleIdAndTypeOrderByCreatedAtDesc(vehicleId, SPEEDING)
                    .map(a -> a.getCreatedAt().isAfter(since))
                    .orElse(false);
            if (!recent) {
                String msg = String.format("%s: tezlik %d km/soat (chegara %d)",
                        v.getName(), Math.round(speedKmh), v.getSpeedLimitKmh());
                alerts.save(new Alert(vehicleId, SPEEDING, msg, lat, lon, time));
            }
        }
        geofences.check(v, lat, lon, time);
        stops.check(v, speedKmh, lat, lon, time);
        return p;
    }

    @Transactional(readOnly = true)
    public List<LiveVehicleDto> live() {
        Instant now = Instant.now();
        List<LiveVehicleDto> out = new ArrayList<>();
        for (Vehicle v : vehicles.findAll()) {
            Optional<GpsPoint> last = points.findFirstByVehicleIdOrderByRecordedAtDesc(v.getId());
            if (last.isEmpty()) {
                out.add(new LiveVehicleDto(v.getId(), v.getName(), null, null, 0,
                        "OFFLINE", -1, v.getSpeedLimitKmh()));
                continue;
            }
            GpsPoint p = last.get();
            long age = Duration.between(p.getRecordedAt(), now).getSeconds();
            String status = age > OFFLINE_AFTER_SECONDS ? "OFFLINE"
                    : p.getSpeedKmh() < STOPPED_BELOW_KMH ? "STOPPED" : "MOVING";
            out.add(new LiveVehicleDto(v.getId(), v.getName(), p.getLat(), p.getLon(),
                    Math.round(p.getSpeedKmh()), status, age, v.getSpeedLimitKmh()));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public List<TrackPointDto> track(Long vehicleId, Instant from, Instant to) {
        return points.findByVehicleIdAndRecordedAtBetweenOrderByRecordedAtAsc(vehicleId, from, to)
                .stream()
                .map(p -> new TrackPointDto(p.getLat(), p.getLon(), p.getSpeedKmh(), p.getRecordedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ReportDto report(Long vehicleId, Instant from, Instant to) {
        Vehicle v = vehicles.findById(vehicleId)
                .orElseThrow(() -> new NoSuchElementException("Mashina topilmadi: " + vehicleId));
        List<GpsPoint> pts = points.findByVehicleIdAndRecordedAtBetweenOrderByRecordedAtAsc(vehicleId, from, to);

        double distance = 0, sum = 0, max = 0;
        int speeding = 0;
        GpsPoint prev = null;
        for (GpsPoint p : pts) {
            if (prev != null) {
                distance += DistanceCalcEarth.DIST_EARTH.calcDist(prev.getLat(), prev.getLon(), p.getLat(), p.getLon());
            }
            sum += p.getSpeedKmh();
            max = Math.max(max, p.getSpeedKmh());
            if (p.getSpeedKmh() > v.getSpeedLimitKmh()) {
                speeding++;
            }
            prev = p;
        }
        double avg = pts.isEmpty() ? 0 : sum / pts.size();
        return new ReportDto(v.getId(), v.getName(), from, to, pts.size(),
                Math.round(distance / 10.0) / 100.0, Math.round(avg * 10) / 10.0,
                Math.round(max * 10) / 10.0, speeding);
    }

    @Transactional(readOnly = true)
    public List<AlertDto> recentAlerts() {
        return alerts.findTop50ByOrderByCreatedAtDesc().stream()
                .map(a -> new AlertDto(a.getId(), a.getVehicleId(), a.getType(), a.getMessage(),
                        a.getLat(), a.getLon(), a.getCreatedAt()))
                .toList();
    }
}