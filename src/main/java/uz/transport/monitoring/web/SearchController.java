package uz.transport.monitoring.web;



import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.transport.monitoring.search.GeocodingService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class SearchController {

    private final GeocodingService geocoding;

    public SearchController(GeocodingService geocoding) {
        this.geocoding = geocoding;
    }


    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String q) {
        try {
            return ResponseEntity.ok(geocoding.search(q));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "So'rov to'xtatildi"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Qidiruv xizmati javob bermadi" + e.getMessage()));
        }
    }
}