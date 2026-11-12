package uz.transport.monitoring.repo;



import org.springframework.data.jpa.repository.JpaRepository;
import uz.transport.monitoring.domain.Alert;

import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findTop50ByOrderByCreatedAtDesc();

    Optional<Alert> findFirstByVehicleIdAndTypeOrderByCreatedAtDesc(Long vehicleId, String type);
}