package uz.transport.traffic;

/** Tirbandlik zonasi: markaz koordinatasi va radiusi (metr). */
public record TrafficZone(String name, double lat, double lon, double radiusMeters) {
}
