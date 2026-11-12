package uz.transport.monitoring.sim;


public record VehicleDto(int id, String name, double lat, double lon, long speedKmh, String status) {
}