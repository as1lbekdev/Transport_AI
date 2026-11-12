package uz.transport.monitoring.web;



import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.transport.monitoring.trip.TripService;
import uz.transport.monitoring.trip.TripState;

import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;


@RestController
@RequestMapping(value = "/api/driver", produces = MediaType.APPLICATION_JSON_VALUE)
public class DriverController {

    private final TripService trips;

    public DriverController(TripService trips) {
        this.trips = trips;
    }

    /** Safarni boshlash: qayerdan va qayerga matn ko'rinishida. */
    @PostMapping("/trips")
    public TripState start(@RequestBody StartTripRequest r) {
        return trips.start(r.vehicleId(), r.from(), r.to(), r.lat(), r.lon());
    }

    /** Joylashuvni yuborish. Javobda joriy safar holati (yo'l, masofa, vaqt, xabar) keladi. */
    @PostMapping("/ping")
    public TripState ping(@RequestBody PingRequest r) {
        if (r.vehicleId() == null || r.lat() < -90 || r.lat() > 90 || r.lon() < -180 || r.lon() > 180) {
            throw new IllegalArgumentException("vehicleId yoki koordinata noto'g'ri");
        }
        Instant t = r.timestamp() != null ? r.timestamp() : Instant.now();
        return trips.ping(r.vehicleId(), r.lat(), r.lon(), r.speedKmh(), t);
    }

    @GetMapping("/trips/active")
    public TripState active(@RequestParam Long vehicleId) {
        return trips.state(vehicleId);
    }

    @PostMapping("/trips/cancel")
    public TripState cancel(@RequestParam Long vehicleId) {
        return trips.cancel(vehicleId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> notFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }
}
