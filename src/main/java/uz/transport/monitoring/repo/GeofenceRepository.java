package uz.transport.monitoring.repo;



import org.springframework.data.jpa.repository.JpaRepository;
import uz.transport.monitoring.domain.Geofence;

public interface GeofenceRepository extends JpaRepository<Geofence, Long> {
}