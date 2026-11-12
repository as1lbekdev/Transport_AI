package uz.transport.routing;

import uz.transport.graph.RoadEdge;
import uz.transport.graph.RoadGraph;
import uz.transport.traffic.TrafficService;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

abstract class AbstractRouter implements RoutingAlgorithm {

    /** Priority queue elementi. */
    protected record QItem(int node, double priority) {
    }

    protected final RoadGraph graph;
    protected final TrafficService traffic;

    protected AbstractRouter(RoadGraph graph, TrafficService traffic) {
        this.graph = graph;
        this.traffic = traffic;
    }

    /**
     * Qirra og'irligi = sayohat vaqti (soniya):
     *   w(e) = uzunlik / (erkin_tezlik * tirbandlik_koeffitsienti)
     * Tirbandlik bo'lgan bo'lakda tezlik tushadi, vaqt oshadi, algoritm uni chetlab o'tadi.
     */
    protected double cost(RoadEdge e, boolean trafficAware) {
        double factor = trafficAware ? traffic.congestionFactor(e.midLat(), e.midLon()) : 1.0;
        double speedMs = Math.max(e.freeSpeedKmh() * factor, 5.0) / 3.6;
        return e.lengthMeters() / speedMs;
    }

    protected List<RoadEdge> backtrack(Map<Integer, RoadEdge> prev, int start, int goal) {
        LinkedList<RoadEdge> path = new LinkedList<>();
        int cur = goal;
        while (cur != start) {
            RoadEdge e = prev.get(cur);
            path.addFirst(e);
            cur = e.from();
        }
        return path;
    }
}
