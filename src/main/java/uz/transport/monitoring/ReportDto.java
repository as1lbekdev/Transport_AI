package uz.transport.monitoring;


import java.time.Instant;

public record ReportDto(Long vehicleId, String vehicleName, Instant from, Instant to, int points,
                        double distanceKm, double avgSpeedKmh, double maxSpeedKmh, int speedingPoints) {
}