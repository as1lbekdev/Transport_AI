package uz.transport.monitoring.trip;



import com.graphhopper.util.DistanceCalcEarth;
import org.springframework.stereotype.Service;
import uz.transport.monitoring.MonitoringService;
import uz.transport.monitoring.domain.Alert;
import uz.transport.monitoring.domain.Trip;
import uz.transport.monitoring.repo.AlertRepository;
import uz.transport.monitoring.repo.TripRepository;
import uz.transport.monitoring.repo.VehicleRepository;

import uz.transport.monitoring.search.PlaceDto;
import uz.transport.monitoring.search.PlaceResolver;
import uz.transport.routing.RouteResult;
import uz.transport.routing.RoutingService;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Haydovchi safarining butun mantiqi: yo'l hisoblash, yo'ldan chiqishni aniqlash,
 * qayta marshrutlash va manzilga yetib kelishni aniqlash.
 */
@Service
public class TripService {

    private static final String ACTIVE = "ACTIVE";
    private static final double ARRIVAL_METERS = 40;      // manzilga yetdi
    private static final double OFF_ROUTE_METERS = 60;    // yo'ldan chiqdi
    private static final long REFRESH_SECONDS = 30;       // tirbandlik o'zgarishi uchun yangilash

    private record Live(RouteResult route, Instant routedAt) {
    }

    private final TripRepository trips;
    private final VehicleRepository vehicles;
    private final AlertRepository alerts;
    private final MonitoringService monitoring;
    private final RoutingService routing;
    private final PlaceResolver places;
    /** Faol safarlarning joriy marshruti (xotirada). */
    private final Map<Long, Live> live = new ConcurrentHashMap<>();

    public TripService(TripRepository trips, VehicleRepository vehicles, AlertRepository alerts,
                       MonitoringService monitoring, RoutingService routing, PlaceResolver places) {
        this.trips = trips;
        this.vehicles = vehicles;
        this.alerts = alerts;
        this.monitoring = monitoring;
        this.routing = routing;
        this.places = places;
    }

    /** Yangi safar: joylarni aniqlaydi, eng tez yo'lni hisoblaydi. */
    public TripState start(Long vehicleId, String fromText, String toText, Double lat, Double lon) {
        if (vehicleId == null || !vehicles.existsById(vehicleId)) {
            throw new NoSuchElementException("Mashina topilmadi: " + vehicleId);
        }
        if (toText == null || toText.isBlank()) {
            throw new IllegalArgumentException("Manzilni kiriting");
        }
        cancel(vehicleId);

        PlaceDto dest = places.resolve(toText.trim())
                .orElseThrow(() -> new IllegalArgumentException("Joy topilmadi: " + toText));

        boolean follow = fromText == null || fromText.isBlank()
                || "mening joylashuvim".equalsIgnoreCase(fromText.trim());
        String originName;
        double originLat, originLon;
        if (follow) {
            if (lat == null || lon == null) {
                throw new IllegalArgumentException("Joylashuv hali aniqlanmadi");
            }
            originName = "Mening joylashuvim";
            originLat = lat;
            originLon = lon;
        } else {
            PlaceDto o = places.resolve(fromText.trim())
                    .orElseThrow(() -> new IllegalArgumentException("Joy topilmadi: " + fromText));
            originName = o.name();
            originLat = o.lat();
            originLon = o.lon();
        }

        Trip t = trips.save(new Trip(vehicleId, originName, originLat, originLon,
                dest.name(), dest.lat(), dest.lon(), follow));
        Optional<RouteResult> r = routing.route("astar", originLat, originLon, dest.lat(), dest.lon(), true);
        if (r.isEmpty()) {
            finish(t, "CANCELLED");
            throw new IllegalArgumentException("Yo'l topilmadi");
        }
        live.put(t.getId(), new Live(r.get(), Instant.now()));
        return build(t, r.get(), "Yo'l hisoblandi");
    }

    /** Har bir GPS nuqta shu yerga keladi: saqlanadi va safar holati yangilanadi. */
    public TripState ping(Long vehicleId, double lat, double lon, double speedKmh, Instant time) {
        monitoring.recordPoint(vehicleId, lat, lon, speedKmh, time);

        Optional<Trip> opt = trips.findFirstByVehicleIdAndStatusOrderByStartedAtDesc(vehicleId, ACTIVE);
        if (opt.isEmpty()) {
            return TripState.idle(null);
        }
        Trip t = opt.get();
        Live l = live.get(t.getId());

        // Boshqa joydan reja qilingan yo'l: mashina holatiga bog'liq emas
        if (!t.isFollowVehicle()) {
            return l != null ? build(t, l.route(), null) : reroute(t, t.getOriginLat(), t.getOriginLon(), null);
        }

        double toDest = DistanceCalcEarth.DIST_EARTH.calcDist(lat, lon, t.getDestLat(), t.getDestLon());
        if (toDest <= ARRIVAL_METERS) {
            finish(t, "FINISHED");
            return new TripState(false, "FINISHED", "Manzilga yetib keldingiz: " + t.getDestName(),
                    t.getOriginName(), t.getDestName(), null, null, List.of(), t.getDestLat(), t.getDestLon());
        }
        if (l == null) {                                   // server qayta ishga tushgan
            return reroute(t, lat, lon, "Yo'l qayta tiklandi");
        }
        double off = GeoUtil.distanceToPolylineMeters(lat, lon, l.route().points());
        if (off > OFF_ROUTE_METERS) {
            alerts.save(new Alert(vehicleId, "OFF_ROUTE",
                    String.format("Mashina yo'ldan %d m uzoqlashdi, yo'l qayta hisoblandi", Math.round(off)),
                    lat, lon, time));
            return reroute(t, lat, lon, "Yo'ldan chiqdingiz: yo'l qayta hisoblandi");
        }
        if (Duration.between(l.routedAt(), Instant.now()).getSeconds() >= REFRESH_SECONDS) {
            return reroute(t, lat, lon, null);              // tirbandlik o'zgargan bo'lishi mumkin
        }
        return build(t, l.route(), null);
    }

    public TripState state(Long vehicleId) {
        Optional<Trip> opt = trips.findFirstByVehicleIdAndStatusOrderByStartedAtDesc(vehicleId, ACTIVE);
        if (opt.isEmpty()) {
            return TripState.idle(null);
        }
        Live l = live.get(opt.get().getId());
        return l != null ? build(opt.get(), l.route(), null) : TripState.idle("Safar bor, yo'l hali hisoblanmagan");
    }

    public TripState cancel(Long vehicleId) {
        trips.findFirstByVehicleIdAndStatusOrderByStartedAtDesc(vehicleId, ACTIVE)
                .ifPresent(t -> finish(t, "CANCELLED"));
        return TripState.idle(null);
    }

    private TripState reroute(Trip t, double lat, double lon, String message) {
        Optional<RouteResult> r = routing.route("astar", lat, lon, t.getDestLat(), t.getDestLon(), true);
        Live old = live.get(t.getId());
        if (r.isEmpty()) {                                  // yangi yo'l topilmadi, eskisi qoladi
            return old != null ? build(t, old.route(), "Yo'lni yangilab bo'lmadi") : TripState.idle("Yo'l topilmadi");
        }
        live.put(t.getId(), new Live(r.get(), Instant.now()));
        return build(t, r.get(), message);
    }

    private void finish(Trip t, String status) {
        t.finish(status);
        trips.save(t);
        live.remove(t.getId());
    }

    private TripState build(Trip t, RouteResult r, String message) {
        return new TripState(true, ACTIVE, message, t.getOriginName(), t.getDestName(),
                r.distanceMeters(), r.timeSeconds(), r.points(), t.getDestLat(), t.getDestLon());
    }
}
