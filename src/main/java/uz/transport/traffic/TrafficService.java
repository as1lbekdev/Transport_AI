package uz.transport.traffic;

import java.util.List;

/**
 * Tirbandlik ma'lumoti manbai. Hozir simulyatsiya, keyin haqiqiy GPS ma'lumotidan
 * hisoblanadigan implementatsiya bilan almashtiriladi (marshrut kodi o'zgarmaydi).
 */
public interface TrafficService {

    /** Berilgan nuqtada tezlik koeffitsienti: 1.0 = yo'l bo'sh, 0.2 = qattiq tirbandlik. */
    double congestionFactor(double lat, double lon);

    List<TrafficZone> zones();
}
