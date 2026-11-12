package uz.transport.monitoring.search;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Joy nomini koordinataga aylantiradi (geokodlash). Manba: OpenStreetMap Nominatim.
 * Ommaviy xizmat qoidalari: sekundiga 1 ta so'rov, aniqlovchi User-Agent, natijani keshlash.
 */
@Service
public class GeocodingService {

    private static final String BASE = "https://nominatim.openstreetmap.org/search";
    /** Toshkent hududi: chap(minLon), yuqori(maxLat), o'ng(maxLon), pastki(minLat). */
    private static final String VIEWBOX = "69.10,41.42,69.45,41.18";

    /** Qisqartmalar. Kerak bo'lsa yangilarini qo'shing (kichik harflarda). */
    private static final Map<String, String> ALIASES = Map.of(
            "tatu", "Toshkent axborot texnologiyalari universiteti");

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, List<PlaceDto>> cache = new ConcurrentHashMap<>();
    private long lastCall = 0;

    public synchronized List<PlaceDto> search(String query) throws IOException, InterruptedException {
        String q = query == null ? "" : query.trim();
        if (q.length() < 2) {
            return List.of();
        }
        String key = q.toLowerCase(Locale.ROOT);
        List<PlaceDto> cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        String effective = ALIASES.getOrDefault(key, q);
        long wait = 1100 - (System.currentTimeMillis() - lastCall);
        if (wait > 0) {
            Thread.sleep(wait);
        }

        String url = BASE + "?q=" + URLEncoder.encode(effective, StandardCharsets.UTF_8)
                + "&format=jsonv2&limit=5&viewbox=" + VIEWBOX + "&bounded=1&accept-language=uz,ru,en";
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .header("User-Agent", "TransportMonitoringDiplom/1.0 (student project)")
                .build();
        lastCall = System.currentTimeMillis();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IOException("Nominatim javobi: " + resp.statusCode());
        }

        List<PlaceDto> out = new ArrayList<>();
        for (JsonNode n : mapper.readTree(resp.body())) {
            String display = n.path("display_name").asText("");
            String name = n.path("name").asText("");
            if (name.isBlank()) {
                int comma = display.indexOf(',');
                name = comma > 0 ? display.substring(0, comma) : display;
            }
            out.add(new PlaceDto(name, display, n.path("lat").asDouble(), n.path("lon").asDouble(),
                    n.path("type").asText("")));
        }
        cache.put(key, out);
        return out;
    }
}