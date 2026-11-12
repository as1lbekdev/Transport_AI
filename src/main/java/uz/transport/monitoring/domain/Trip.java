package uz.transport.monitoring.domain;


import jakarta.persistence.*;

import java.time.Instant;

/** Haydovchining safari: qayerdan, qayerga va holati (ACTIVE, FINISHED, CANCELLED). */
@Entity
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "origin_name")
    private String originName;
    @Column(name = "origin_lat")
    private double originLat;
    @Column(name = "origin_lon")
    private double originLon;

    @Column(name = "dest_name")
    private String destName;
    @Column(name = "dest_lat")
    private double destLat;
    @Column(name = "dest_lon")
    private double destLon;

    /** true: yo'l mashinaning hozirgi joyidan boshlanadi (kuzatiladi va qayta hisoblanadi). */
    @Column(name = "follow_vehicle")
    private boolean followVehicle;

    private String status;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    protected Trip() {
    }

    public Trip(Long vehicleId, String originName, double originLat, double originLon,
                String destName, double destLat, double destLon, boolean followVehicle) {
        this.vehicleId = vehicleId;
        this.originName = originName;
        this.originLat = originLat;
        this.originLon = originLon;
        this.destName = destName;
        this.destLat = destLat;
        this.destLon = destLon;
        this.followVehicle = followVehicle;
        this.status = "ACTIVE";
        this.startedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getVehicleId() { return vehicleId; }
    public String getOriginName() { return originName; }
    public double getOriginLat() { return originLat; }
    public double getOriginLon() { return originLon; }
    public String getDestName() { return destName; }
    public double getDestLat() { return destLat; }
    public double getDestLon() { return destLon; }
    public boolean isFollowVehicle() { return followVehicle; }
    public String getStatus() { return status; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getFinishedAt() { return finishedAt; }

    public void finish(String newStatus) {
        this.status = newStatus;
        this.finishedAt = Instant.now();
    }
}
