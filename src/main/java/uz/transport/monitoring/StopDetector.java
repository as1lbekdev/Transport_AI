package uz.transport.monitoring;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uz.transport.monitoring.domain.Alert;
import uz.transport.monitoring.domain.Vehicle;
import uz.transport.monitoring.repo.AlertRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Mashina belgilangan vaqtdan ko'p to'xtab tursa, bitta ogohlantirish yaratadi. */
@Component
public class StopDetector {

    private static final double STOPPED_BELOW_KMH = 3.0;

    private static class StopState {
        final Instant since;
        boolean alerted;

        StopState(Instant since) {
            this.since = since;
        }
    }

    private final AlertRepository alerts;
    private final long stopAlertSeconds;
    private final Map<Long, StopState> states = new ConcurrentHashMap<>();

    public StopDetector(AlertRepository alerts,
                        @Value("${monitoring.stop-alert-seconds:300}") long stopAlertSeconds) {
        this.alerts = alerts;
        this.stopAlertSeconds = stopAlertSeconds;
    }

    public void check(Vehicle v, double speedKmh, double lat, double lon, Instant time) {
        if (speedKmh >= STOPPED_BELOW_KMH) {
            states.remove(v.getId());          // mashina yurdi, hisob qaytadan boshlanadi
            return;
        }
        StopState st = states.computeIfAbsent(v.getId(), id -> new StopState(time));
        long secs = Duration.between(st.since, time).getSeconds();
        if (!st.alerted && secs >= stopAlertSeconds) {
            st.alerted = true;
            alerts.save(new Alert(v.getId(), "STOPPED_LONG",
                    String.format("%s: %d soniyadan beri to'xtab turibdi", v.getName(), secs),
                    lat, lon, time));
        }
    }
}