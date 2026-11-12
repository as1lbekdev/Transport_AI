package uz.transport.monitoring.domain;



import jakarta.persistence.*;

import java.time.Instant;


@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "alert_type", nullable = false)
    private String type;

    @Column(length = 300)
    private String message;

    private double lat;
    private double lon;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Alert() {
    }

    public Alert(Long vehicleId, String type, String message, double lat, double lon, Instant createdAt) {
        this.vehicleId = vehicleId;
        this.type = type;
        this.message = message;
        this.lat = lat;
        this.lon = lon;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getVehicleId() { return vehicleId; }
    public String getType() { return type; }
    public String getMessage() { return message; }
    public double getLat() { return lat; }
    public double getLon() { return lon; }
    public Instant getCreatedAt() { return createdAt; }
}