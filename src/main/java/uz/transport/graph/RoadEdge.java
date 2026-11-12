package uz.transport.graph;

/**
 * Yo'l grafidagi yo'naltirilgan qirra (yo'l bo'lagi): from -> to.
 * freeSpeedKmh: tirbandliksiz (OSM bo'yicha) tezlik.
 * midLat/midLon: bo'lak o'rtasi, tirbandlik zonasini aniqlash uchun.
 */
public record RoadEdge(int edgeId, int from, int to, double lengthMeters,
                       double freeSpeedKmh, double midLat, double midLon) {
}
