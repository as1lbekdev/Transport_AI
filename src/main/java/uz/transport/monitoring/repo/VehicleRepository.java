package uz.transport.monitoring.repo;



import org.springframework.data.jpa.repository.JpaRepository;
import uz.transport.monitoring.domain.Vehicle;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByName(String name);
}