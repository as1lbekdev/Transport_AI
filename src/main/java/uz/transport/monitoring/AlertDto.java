package uz.transport.monitoring;


import java.time.Instant;

public record AlertDto(Long id, Long vehicleId, String type, String message,
                       double lat, double lon, Instant createdAt) {
}