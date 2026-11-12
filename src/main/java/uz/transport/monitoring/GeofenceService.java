package uz.transport.monitoring;


import com.graphhopper.util.DistanceCalcEarth;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.transport.monitoring.domain.Alert;
import uz.transport.monitoring.domain.Geofence;
import uz.transport.monitoring.domain.Vehicle;
import uz.transport.monitoring.repo.AlertRepository;
import uz.transport.monitoring.repo.GeofenceRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Zonalarni boshqaradi va mashina zonaga kirganda/chiqqanda ogohlantirish yaratadi. */
@Service
public class GeofenceService {

    private final GeofenceRepository repo;
    private final AlertRepository alerts;
    private volatile List<Geofence> cache;
    /** "mashinaId:zonaId" -> mashina hozir zona ichidami */
    private final Map<String, Boolean> inside = new ConcurrentHashMap<>();

    public GeofenceService(GeofenceRepository repo, AlertRepository alerts) {
        this.repo = repo;
        this.alerts = alerts;
    }

    public List<Geofence> all() {
        List<Geofence> c = cache;
        if (c == null) {
            c = repo.findAll();
            cache = c;
        }
        return c;
    }

    @Transactional
    public Geofence create(String name, double lat, double lon, double radiusMeters) {
        Geofence g = repo.save(new Geofence(name, lat, lon, radiusMeters));
        cache = null;
        return g;
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
        cache = null;
        inside.keySet().removeIf(k -> k.endsWith(":" + id));
    }

    /** Har bir GPS nuqtada chaqiriladi. Holat o'zgargandagina ogohlantirish yaratiladi. */
    public void check(Vehicle v, double lat, double lon, Instant time) {
        for (Geofence g : all()) {
            boolean now = DistanceCalcEarth.DIST_EARTH.calcDist(lat, lon, g.getLat(), g.getLon())
                    <= g.getRadiusMeters();
            Boolean was = inside.put(v.getId() + ":" + g.getId(), now);
            if (was != null && was != now) {   // birinchi kuzatuvda ogohlantirish yo'q
                String msg = String.format("%s: '%s' zonasiga %s", v.getName(), g.getName(),
                        now ? "kirdi" : "chiqdi");
                alerts.save(new Alert(v.getId(), now ? "GEOFENCE_ENTER" : "GEOFENCE_EXIT",
                        msg, lat, lon, time));
            }
        }
    }
}
