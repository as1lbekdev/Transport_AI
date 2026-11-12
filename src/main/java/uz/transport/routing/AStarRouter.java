package uz.transport.routing;

import org.springframework.stereotype.Component;
import uz.transport.graph.RoadEdge;
import uz.transport.graph.RoadGraph;
import uz.transport.traffic.TrafficService;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * A* algoritmi: Dijkstra + maqsad tomon yo'naltiruvchi evristika.
 * Evristika h(n) = to'g'ri chiziq masofasi / maksimal tezlik.
 * Tezlik hech qachon maksimaldan oshmagani va tirbandlik uni faqat kamaytirgani uchun
 * evristika "admissible" (haqiqiy vaqtni kam baholaydi), shuning uchun natija optimal.
 */
@Component
public class AStarRouter extends AbstractRouter {

    private static final double MAX_SPEED_MS = 140.0 / 3.6;

    public AStarRouter(RoadGraph graph, TrafficService traffic) {
        super(graph, traffic);
    }

    @Override
    public String name() {
        return "astar";
    }

    @Override
    public SearchResult search(int start, int goal, boolean trafficAware) {
        Map<Integer, Double> g = new HashMap<>();
        Map<Integer, RoadEdge> prev = new HashMap<>();
        Set<Integer> settled = new HashSet<>();
        PriorityQueue<QItem> pq = new PriorityQueue<>(Comparator.comparingDouble(QItem::priority));

        g.put(start, 0.0);
        pq.add(new QItem(start, heuristic(start, goal)));
        long visited = 0;

        while (!pq.isEmpty()) {
            QItem cur = pq.poll();
            if (!settled.add(cur.node())) {
                continue;
            }
            visited++;
            if (cur.node() == goal) {
                return new SearchResult(true, backtrack(prev, start, goal), visited);
            }
            double gc = g.get(cur.node());
            for (RoadEdge e : graph.outEdges(cur.node())) {
                if (settled.contains(e.to())) {
                    continue;
                }
                double ng = gc + cost(e, trafficAware);
                if (ng < g.getOrDefault(e.to(), Double.MAX_VALUE)) {
                    g.put(e.to(), ng);
                    prev.put(e.to(), e);
                    pq.add(new QItem(e.to(), ng + heuristic(e.to(), goal)));
                }
            }
        }
        return SearchResult.notFound(visited);
    }

    private double heuristic(int node, int goal) {
        return graph.straightDistance(node, goal) / MAX_SPEED_MS;
    }
}
