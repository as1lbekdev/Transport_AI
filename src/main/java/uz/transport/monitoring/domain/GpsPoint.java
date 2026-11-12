package uz.transport.monitoring.domain;



import jakarta.persistence.*;

import java.time.Instant;

/** Mashinadan kelgan bitta GPS nuqta. */
@Entity
@Table(name = "gps_points",
        indexes = @Index(name = "idx_gps_vehicle_time", columnList = "vehicle_id, recorded_at"))
public class GpsPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    private double lat;
    private double lon;

    @Column(name = "speed_kmh")
    private double speedKmh;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    protected GpsPoint() {
    }

    public GpsPoint(Long vehicleId, double lat, double lon, double speedKmh, Instant recordedAt) {
        this.vehicleId = vehicleId;
        this.lat = lat;
        this.lon = lon;
        this.speedKmh = speedKmh;
        this.recordedAt = recordedAt;
    }

    public Long getId() { return id; }
    public Long getVehicleId() { return vehicleId; }
    public double getLat() { return lat; }
    public double getLon() { return lon; }
    public double getSpeedKmh() { return speedKmh; }
    public Instant getRecordedAt() { return recordedAt; }
}