package uz.transport.monitoring;



public record LiveVehicleDto(Long id, String name, Double lat, Double lon, double speedKmh,
                             String status, long lastSeenSecondsAgo, int speedLimitKmh) {
}
