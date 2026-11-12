package uz.transport.monitoring.search;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/** Joy nomini bitta aniq joyga aylantiradi (birinchi natija). */
@Service
public class PlaceResolver {

    private final GeocodingService geocoding;
    // Agar "Saqlash" (SavedPlace) qismini qo'shgan bo'lsangiz, shu ikki qatorni oching:
    // private final LocalPlaceService local;

    public PlaceResolver(GeocodingService geocoding /*, LocalPlaceService local */) {
        this.geocoding = geocoding;
        // this.local = local;
    }

    public Optional<PlaceDto> resolve(String text) {
        // List<PlaceDto> saved = local.search(text);
        // if (!saved.isEmpty()) return Optional.of(saved.get(0));
        try {
            List<PlaceDto> found = geocoding.search(text);
            return found.stream().findFirst();
        } catch (IOException e) {
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }
}