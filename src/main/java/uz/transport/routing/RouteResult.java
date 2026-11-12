package uz.transport.routing;

import java.util.List;

/**
 * Marshrut natijasi.
 * timeSeconds     - kutilayotgan sayohat vaqti (tirbandlikni hisobga olgan holda; GraphHopper uchun tirbandliksiz).
 * freeFlowSeconds - tirbandliksiz sayohat vaqti.
 * visitedNodes    - algoritm ko'rgan tugunlar soni (GraphHopper uchun -1).
 * computeMillis   - qidiruv vaqti (millisekund).
 * points          - [lat, lon] nuqtalar ro'yxati.
 */
public record RouteResult(String algorithm, boolean trafficAware, double distanceMeters,
                          double timeSeconds, double freeFlowSeconds, long visitedNodes,
                          double computeMillis, List<double[]> points) {
}
