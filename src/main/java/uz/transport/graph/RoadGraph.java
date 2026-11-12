package uz.transport.graph;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PointList;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter qatlami: GraphHopper'ning ichki grafini bizning oddiy interfeysga o'giradi.
 * Marshrut algoritmlari (Dijkstra, A*) faqat shu klassga tayanadi, GraphHopper'ni bilmaydi.
 */
@Component
public class RoadGraph {

    private final BaseGraph graph;
    private final NodeAccess nodes;
    private final LocationIndex index;
    private final BooleanEncodedValue accessEnc;
    private final DecimalEncodedValue speedEnc;
    private final EdgeFilter snapFilter;
    private final ThreadLocal<EdgeExplorer> explorer;

    public RoadGraph(GraphHopper hopper) {
        this.graph = hopper.getBaseGraph();
        this.nodes = graph.getNodeAccess();
        this.index = hopper.getLocationIndex();
        EncodingManager em = hopper.getEncodingManager();
        this.accessEnc = em.getBooleanEncodedValue("car_access");
        this.speedEnc = em.getDecimalEncodedValue("car_average_speed");
        // Koordinatani avtomobil yuradigan yo'lga bog'lash (har ikki yo'nalish)
        this.snapFilter = e -> e.get(accessEnc) || e.getReverse(accessEnc);
        // Faqat chiqish yo'nalishida avtomobil yura oladigan qirralar
        EdgeFilter outFilter = e -> e.get(accessEnc);
        this.explorer = ThreadLocal.withInitial(() -> graph.createEdgeExplorer(outFilter));
    }

    public int nodeCount() {
        return graph.getNodes();
    }

    public double lat(int node) {
        return nodes.getLat(node);
    }

    public double lon(int node) {
        return nodes.getLon(node);
    }

    /** Koordinataga eng yaqin yo'l tuguni; topilmasa -1. */
    public int snap(double lat, double lon) {
        Snap s = index.findClosest(lat, lon, snapFilter);
        return s.isValid() ? s.getClosestNode() : -1;
    }

    /** Tugundan chiqadigan qirralar. */
    public List<RoadEdge> outEdges(int node) {
        List<RoadEdge> list = new ArrayList<>(4);
        EdgeIterator it = explorer.get().setBaseNode(node);
        while (it.next()) {
            double speed = it.get(speedEnc);
            if (speed <= 0) {
                continue;
            }
            int to = it.getAdjNode();
            list.add(new RoadEdge(it.getEdge(), node, to, it.getDistance(), speed,
                    (lat(node) + lat(to)) / 2.0, (lon(node) + lon(to)) / 2.0));
        }
        return list;
    }

    /** Ikki tugun orasidagi to'g'ri chiziq masofasi (metr), A* evristikasi uchun. */
    public double straightDistance(int a, int b) {
        return DistanceCalcEarth.DIST_EARTH.calcDist(lat(a), lon(a), lat(b), lon(b));
    }

    /** Qirraning haqiqiy geometriyasi (egri chiziq nuqtalari bilan). */
    public PointList geometry(RoadEdge e) {
        return graph.getEdgeIteratorState(e.edgeId(), e.to()).fetchWayGeometry(FetchMode.ALL);
    }
}
