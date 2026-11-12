

package uz.transport.monitoring.web;

import java.time.Instant;

public record GpsRequest(Long vehicleId, double lat, double lon, double speedKmh, Instant timestamp) {
}
