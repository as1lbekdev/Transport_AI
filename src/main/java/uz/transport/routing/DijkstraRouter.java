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

/** Klassik Dijkstra algoritmi. Murakkabligi: O((V + E) log V). */
@Component
public class DijkstraRouter extends AbstractRouter {

    public DijkstraRouter(RoadGraph graph, TrafficService traffic) {
        super(graph, traffic);
    }

    @Override
    public String name() {
        return "dijkstra";
    }

    @Override
    public SearchResult search(int start, int goal, boolean trafficAware) {
        Map<Integer, Double> dist = new HashMap<>();
        Map<Integer, RoadEdge> prev = new HashMap<>();
        Set<Integer> settled = new HashSet<>();
        PriorityQueue<QItem> pq = new PriorityQueue<>(Comparator.comparingDouble(QItem::priority));

        dist.put(start, 0.0);
        pq.add(new QItem(start, 0.0));
        long visited = 0;

        while (!pq.isEmpty()) {
            QItem cur = pq.poll();
            if (!settled.add(cur.node())) {
                continue;               // bu tugun allaqachon yakunlangan
            }
            visited++;
            if (cur.node() == goal) {
                return new SearchResult(true, backtrack(prev, start, goal), visited);
            }
            double d = dist.get(cur.node());
            for (RoadEdge e : graph.outEdges(cur.node())) {
                if (settled.contains(e.to())) {
                    continue;
                }
                double nd = d + cost(e, trafficAware);
                if (nd < dist.getOrDefault(e.to(), Double.MAX_VALUE)) {
                    dist.put(e.to(), nd);
                    prev.put(e.to(), e);
                    pq.add(new QItem(e.to(), nd));
                }
            }
        }
        return SearchResult.notFound(visited);
    }
}
