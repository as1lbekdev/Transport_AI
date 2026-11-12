package uz.transport.monitoring.domain;



import jakarta.persistence.*;

/** Monitoring qilinadigan transport vositasi. */
@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String plate;

    @Column(name = "speed_limit_kmh")
    private int speedLimitKmh;

    protected Vehicle() {
    }

    public Vehicle(String name, String plate, int speedLimitKmh) {
        this.name = name;
        this.plate = plate;
        this.speedLimitKmh = speedLimitKmh;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPlate() { return plate; }
    public int getSpeedLimitKmh() { return speedLimitKmh; }
}
