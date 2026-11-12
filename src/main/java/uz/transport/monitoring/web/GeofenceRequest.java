package uz.transport.monitoring.web;


public record GeofenceRequest(String name, double lat, double lon, double radiusMeters) {
}