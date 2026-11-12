

package uz.transport.monitoring.search;

/** Qidiruv natijasi: joy nomi, to'liq manzil va koordinata. */
public record PlaceDto(String name, String displayName, double lat, double lon, String type) {
}
