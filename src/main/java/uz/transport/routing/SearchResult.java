package uz.transport.routing;

import uz.transport.graph.RoadEdge;

import java.util.List;

/** Algoritm natijasi: topilgan qirralar ketma-ketligi va ko'rilgan (qayta ishlangan) tugunlar soni. */
public record SearchResult(boolean found, List<RoadEdge> path, long visitedNodes) {

    public static SearchResult notFound(long visited) {
        return new SearchResult(false, List.of(), visited);
    }
}
