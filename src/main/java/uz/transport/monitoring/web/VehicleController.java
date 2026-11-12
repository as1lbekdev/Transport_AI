package uz.transport.monitoring.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.transport.monitoring.sim.VehicleDto;
import uz.transport.monitoring.sim.VehicleSimulator;
import uz.transport.traffic.TrafficService;
import uz.transport.traffic.TrafficZone;

import java.util.List;

@RestController
@RequestMapping("/api")
public class VehicleController {

    private final VehicleSimulator simulator;
    private final TrafficService traffic;

    public VehicleController(VehicleSimulator simulator, TrafficService traffic) {
        this.simulator = simulator;
        this.traffic = traffic;
    }

    @GetMapping("/vehicles")
    public List<VehicleDto> vehicles() {
        return simulator.snapshot();
    }

    @GetMapping("/traffic-zones")
    public List<TrafficZone> zones() {
        return traffic.zones();
    }
}
