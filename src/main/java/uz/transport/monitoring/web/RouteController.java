package uz.transport.monitoring.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.transport.routing.RouteResult;
import uz.transport.routing.RoutingService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class RouteController {

    private final RoutingService routing;

    public RouteController(RoutingService routing) {
        this.routing = routing;
    }


    @GetMapping("/route")
    public ResponseEntity<?> route(@RequestParam double fromLat,
                                   @RequestParam double fromLon,
                                   @RequestParam double toLat,
                                   @RequestParam double toLon,
                                   @RequestParam(defaultValue = "astar") String algorithm,
                                   @RequestParam(defaultValue = "true") boolean traffic) {
        if (!routing.supports(algorithm)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Noma'lum algoritm: " + algorithm));
        }
        Optional<RouteResult> result = routing.route(algorithm, fromLat, fromLon, toLat, toLon, traffic);
        return result.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().body(Map.of("error", "Yo'l topilmadi")));
    }


    @GetMapping("/route/compare")
    public ResponseEntity<?> compare(@RequestParam double fromLat,
                                     @RequestParam double fromLon,
                                     @RequestParam double toLat,
                                     @RequestParam double toLon) {
        List<RouteResult> results = routing.compare(fromLat, fromLon, toLat, toLon);
        if (results.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Yo'l topilmadi"));
        }
        return ResponseEntity.ok(results);
    }
}
