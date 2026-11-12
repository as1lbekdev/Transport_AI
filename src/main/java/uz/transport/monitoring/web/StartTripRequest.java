package uz.transport.monitoring.web;


/** from bo'sh yoki "Mening joylashuvim" bo'lsa, yo'l telefonning hozirgi joyidan boshlanadi (lat, lon kerak). */
public record StartTripRequest(Long vehicleId, String from, String to, Double lat, Double lon) {
}
