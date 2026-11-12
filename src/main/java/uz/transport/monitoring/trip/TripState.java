package uz.transport.monitoring.trip;



import java.util.List;

public record TripState(boolean active, String status, String message, String from, String to,
                        Double distanceMeters, Double etaSeconds, List<double[]> points,
                        Double destLat, Double destLon) {

    public static TripState idle(String message) {
        return new TripState(false, "IDLE", message, null, null, null, null, List.of(), null, null);
    }
}
