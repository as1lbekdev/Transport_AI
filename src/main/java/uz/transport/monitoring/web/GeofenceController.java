package uz.transport.monitoring.web;



import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.transport.monitoring.GeofenceService;
import uz.transport.monitoring.domain.Geofence;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/monitoring/geofences", produces = MediaType.APPLICATION_JSON_VALUE)
public class GeofenceController {

    private final GeofenceService service;

    public GeofenceController(GeofenceService service) {
        this.service = service;
    }

    @GetMapping
    public List<Geofence> list() {
        return service.all();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody GeofenceRequest r) {
        if (r.name() == null || r.name().isBlank() || r.radiusMeters() < 20 || r.radiusMeters() > 20000) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "name kerak, radius 20 dan 20000 metrgacha bo'lsin"));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(r.name(), r.lat(), r.lon(), r.radiusMeters()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}