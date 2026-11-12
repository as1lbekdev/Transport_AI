package uz.transport.monitoring;


import java.time.Instant;

public record TrackPointDto(double lat, double lon, double speedKmh, Instant time) {
}