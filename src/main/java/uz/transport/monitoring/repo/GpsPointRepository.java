package uz.transport.monitoring.repo;



import org.springframework.data.jpa.repository.JpaRepository;
import uz.transport.monitoring.domain.GpsPoint;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface GpsPointRepository extends JpaRepository<GpsPoint, Long> {

    Optional<GpsPoint> findFirstByVehicleIdOrderByRecordedAtDesc(Long vehicleId);

    List<GpsPoint> findByVehicleIdAndRecordedAtBetweenOrderByRecordedAtAsc(Long vehicleId, Instant from, Instant to);
}