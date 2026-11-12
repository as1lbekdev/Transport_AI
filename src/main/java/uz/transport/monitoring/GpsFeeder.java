package uz.transport.monitoring;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.transport.monitoring.sim.VehicleDto;
import uz.transport.monitoring.sim.VehicleSimulator;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Component
@ConditionalOnProperty(name = "monitoring.feeder.enabled", havingValue = "true", matchIfMissing = true)
public class GpsFeeder {

    private static final int DEMO_SPEED_LIMIT_KMH = 50;

    private final VehicleSimulator simulator;
    private final MonitoringService monitoring;
    private final Map<String, Long> ids = new ConcurrentHashMap<>();

    public GpsFeeder(VehicleSimulator simulator, MonitoringService monitoring) {
        this.simulator = simulator;
        this.monitoring = monitoring;
    }

    @Scheduled(fixedRate = 2000, initialDelay = 5000)
    public void feed() {
        for (VehicleDto d : simulator.snapshot()) {
            Long id = ids.computeIfAbsent(d.name(),
                    n -> monitoring.ensureVehicle(n, DEMO_SPEED_LIMIT_KMH).getId());
            monitoring.recordPoint(id, d.lat(), d.lon(), d.speedKmh(), Instant.now());
        }
    }
}
