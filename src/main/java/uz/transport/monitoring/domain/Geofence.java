package uz.transport.monitoring.domain;

import jakarta.persistence.*;

/** Doira ko'rinishidagi zona (ombor, mijoz manzili va hokazo). */
@Entity
@Table(name = "geofences")
public class Geofence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private double lat;
    private double lon;

    @Column(name = "radius_m")
    private double radiusMeters;

    protected Geofence() {
    }

    public Geofence(String name, double lat, double lon, double radiusMeters) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.radiusMeters = radiusMeters;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public double getLat() { return lat; }
    public double getLon() { return lon; }
    public double getRadiusMeters() { return radiusMeters; }
}
