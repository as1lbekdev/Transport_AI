package uz.transport.routing;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;
import org.springframework.stereotype.Service;
import uz.transport.graph.RoadEdge;
import uz.transport.graph.RoadGraph;
import uz.transport.traffic.TrafficService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RoutingService {

    private final RoadGraph graph;
    private final TrafficService traffic;
    private final GraphHopper hopper;
    private final Map<String, RoutingAlgorithm> algorithms;

    public RoutingService(RoadGraph graph, TrafficService traffic, GraphHopper hopper,
                          List<RoutingAlgorithm> algorithmList) {
        this.graph = graph;
        this.traffic = traffic;
        this.hopper = hopper;
        this.algorithms = algorithmList.stream()
                .collect(Collectors.toMap(RoutingAlgorithm::name, Function.identity()));
    }

    public boolean supports(String algorithm) {
        return "graphhopper".equals(algorithm) || algorithms.containsKey(algorithm);
    }

    public Optional<RouteResult> route(String algorithm, double fromLat, double fromLon,
                                       double toLat, double toLon, boolean trafficAware) {
        if ("graphhopper".equals(algorithm)) {
            return routeWithGraphHopper(fromLat, fromLon, toLat, toLon);
        }
        RoutingAlgorithm algo = algorithms.get(algorithm);
        int start = graph.snap(fromLat, fromLon);
        int goal = graph.snap(toLat, toLon);
        if (algo == null || start < 0 || goal < 0) {
            return Optional.empty();
        }

        long t0 = System.nanoTime();
        SearchResult sr = algo.search(start, goal, trafficAware);
        double millis = (System.nanoTime() - t0) / 1_000_000.0;
        if (!sr.found()) {
            return Optional.empty();
        }
        return Optional.of(buildResult(algo.name(), trafficAware, sr, millis));
    }

    /** Bir xil so'rovni barcha algoritmlar va rejimlar bilan hisoblaydi (diplom tajribasi uchun). */
    public List<RouteResult> compare(double fromLat, double fromLon, double toLat, double toLon) {
        List<RouteResult> out = new ArrayList<>();
        route("graphhopper", fromLat, fromLon, toLat, toLon, false).ifPresent(out::add);
        for (String name : List.of("dijkstra", "astar")) {
            for (boolean aware : new boolean[]{false, true}) {
                route(name, fromLat, fromLon, toLat, toLon, aware).ifPresent(out::add);
            }
        }
        return out;
    }

    private RouteResult buildResult(String name, boolean aware, SearchResult sr, double millis) {
        double distance = 0, freeFlow = 0, withTraffic = 0;
        List<double[]> points = new ArrayList<>();
        boolean first = true;
        for (RoadEdge e : sr.path()) {
            double freeMs = Math.max(e.freeSpeedKmh(), 5.0) / 3.6;
            double factor = traffic.congestionFactor(e.midLat(), e.midLon());
            double trafficMs = Math.max(e.freeSpeedKmh() * factor, 5.0) / 3.6;
            distance += e.lengthMeters();
            freeFlow += e.lengthMeters() / freeMs;
            withTraffic += e.lengthMeters() / trafficMs;

            PointList geo = graph.geometry(e);
            for (int i = first ? 0 : 1; i < geo.size(); i++) {
                points.add(new double[]{geo.getLat(i), geo.getLon(i)});
            }
            first = false;
        }
        return new RouteResult(name, aware, distance, withTraffic, freeFlow,
                sr.visitedNodes(), millis, points);
    }

    private Optional<RouteResult> routeWithGraphHopper(double fromLat, double fromLon,
                                                       double toLat, double toLon) {
        long t0 = System.nanoTime();
        GHResponse rsp = hopper.route(new GHRequest(fromLat, fromLon, toLat, toLon)
                .setProfile("car").setLocale(Locale.ENGLISH));
        double millis = (System.nanoTime() - t0) / 1_000_000.0;
        if (rsp.hasErrors()) {
            return Optional.empty();
        }
        ResponsePath best = rsp.getBest();
        List<double[]> points = new ArrayList<>();
        for (GHPoint3D p : best.getPoints()) {
            points.add(new double[]{p.getLat(), p.getLon()});
        }
        double seconds = best.getTime() / 1000.0;
        return Optional.of(new RouteResult("graphhopper", false, best.getDistance(),
                seconds, seconds, -1, millis, points));
    }
}
