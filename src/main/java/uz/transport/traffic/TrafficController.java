package uz.transport.traffic;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.transport.routing.RouteResult;
import uz.transport.routing.RoutingService;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/traffic", produces = MediaType.APPLICATION_JSON_VALUE)
public class TrafficController {

    private final TrafficService traffic;
    private final RoutingService routing;

    public TrafficController(TrafficService traffic, RoutingService routing) {
        this.traffic = traffic;
        this.routing = routing;
    }


    @GetMapping("/check")
    public Map<String, Object> check(@RequestParam double lat, @RequestParam double lon) {
        double f = traffic.congestionFactor(lat, lon);
        String level = f > 0.7 ? "bo'sh" : f > 0.4 ? "o'rtacha" : "tirbandlik";
        return Map.of(
                "lat", lat,
                "lon", lon,
                "factor", Math.round(f * 100) / 100.0,
                "level", level,
                "hasTraffic", f <= 0.7);
    }

    @GetMapping("/check-route")
    public ResponseEntity<?> checkRoute(@RequestParam double fromLat,
                                        @RequestParam double fromLon,
                                        @RequestParam double toLat,
                                        @RequestParam double toLon) {
        Optional<RouteResult> r = routing.route("astar", fromLat, fromLon, toLat, toLon, false);
        if (r.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Yo'l topilmadi"));
        }
        RouteResult x = r.get();
        double delaySec = x.timeSeconds() - x.freeFlowSeconds();
        boolean has = delaySec > 30;
        return ResponseEntity.ok(Map.of(
                "hasTraffic", has,
                "freeFlowMinutes", Math.round(x.freeFlowSeconds() / 6.0) / 10.0,
                "withTrafficMinutes", Math.round(x.timeSeconds() / 6.0) / 10.0,
                "delayMinutes", Math.round(delaySec / 6.0) / 10.0,
                "message", has
                        ? String.format("Yo'lda tirbandlik bor: taxminan %.0f daqiqa kechikish", delaySec / 60)
                        : "Yo'l bo'sh"));
    }
}