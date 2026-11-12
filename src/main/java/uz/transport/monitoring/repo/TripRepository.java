package uz.transport.monitoring.repo;


import org.springframework.data.jpa.repository.JpaRepository;
import uz.transport.monitoring.domain.Trip;

import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

    Optional<Trip> findFirstByVehicleIdAndStatusOrderByStartedAtDesc(Long vehicleId, String status);
}