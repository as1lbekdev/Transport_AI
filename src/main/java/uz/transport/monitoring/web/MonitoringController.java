package uz.transport.monitoring.web;



import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.transport.monitoring.*;
import uz.transport.monitoring.domain.Vehicle;


import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class MonitoringController {

    private final MonitoringService monitoring;

    public MonitoringController(MonitoringService monitoring) {
        this.monitoring = monitoring;
    }

    @PostMapping("/gps")
    public ResponseEntity<?> gps(@RequestBody GpsRequest r) {
        if (r.vehicleId() == null || r.lat() < -90 || r.lat() > 90 || r.lon() < -180 || r.lon() > 180) {
            return ResponseEntity.badRequest().body(Map.of("error", "vehicleId yoki koordinata noto'g'ri"));
        }
        Instant t = r.timestamp() != null ? r.timestamp() : Instant.now();
        monitoring.recordPoint(r.vehicleId(), r.lat(), r.lon(), r.speedKmh(), t);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping("/monitoring/live")
    public List<LiveVehicleDto> live() {
        return monitoring.live();
    }

    @GetMapping("/monitoring/vehicles")
    public List<Map<String, Object>> vehicles() {
        return monitoring.allVehicles().stream()
                .map(v -> Map.<String, Object>of("id", v.getId(), "name", v.getName(),
                        "plate", v.getPlate() == null ? "" : v.getPlate(),
                        "speedLimitKmh", v.getSpeedLimitKmh()))
                .toList();
    }

    @PostMapping("/monitoring/vehicles")
    public ResponseEntity<?> createVehicle(@RequestBody VehicleRequest r) {
        if (r.name() == null || r.name().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "name kerak"));
        }
        Vehicle v = monitoring.createVehicle(r.name(), r.plate() == null ? "" : r.plate(),
                r.speedLimitKmh() == null ? 60 : r.speedLimitKmh());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", v.getId(), "name", v.getName()));
    }

    @GetMapping("/monitoring/vehicles/{id}/track")
    public List<TrackPointDto> track(@PathVariable Long id,
                                     @RequestParam(defaultValue = "60") long minutes) {
        Instant to = Instant.now();
        return monitoring.track(id, to.minus(Duration.ofMinutes(minutes)), to);
    }

    @GetMapping("/monitoring/vehicles/{id}/report")
    public ReportDto report(@PathVariable Long id,
                            @RequestParam(defaultValue = "60") long minutes) {
        Instant to = Instant.now();
        return monitoring.report(id, to.minus(Duration.ofMinutes(minutes)), to);
    }

    @GetMapping("/monitoring/alerts")
    public List<AlertDto> alerts() {
        return monitoring.recentAlerts();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> notFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }
}