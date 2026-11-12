package uz.transport.monitoring.web;


public record VehicleRequest(String name, String plate, Integer speedLimitKmh) {
}