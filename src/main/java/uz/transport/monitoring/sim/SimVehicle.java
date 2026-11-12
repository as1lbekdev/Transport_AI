package uz.transport.monitoring.sim;



import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.PointList;

public class SimVehicle {

    public int id;
    public String name;
    public double freeSpeedKmh;

    public double lat;
    public double lon;
    public double speedKmh;

    public double distance;
    public double total;
    public boolean hasPosition = false;

    public PointList path;
    public double[] cum;
    public int seg = 0;

    public SimVehicle(int id, String name, double freeSpeedKmh) {
        this.id = id;
        this.name = name;
        this.freeSpeedKmh = freeSpeedKmh;
        this.speedKmh = freeSpeedKmh;
    }

    public void setPath(PointList pts) {
        this.path = pts;
        this.seg = 0;
        this.distance = 0.0;

        int size = pts.size();
        this.cum = new double[size];
        this.cum[0] = 0.0;

        for (int i = 0; i < size - 1; i++) {
            double segmentDist = DistanceCalcEarth.DIST_EARTH.calcDist(
                    pts.getLat(i), pts.getLon(i),
                    pts.getLat(i + 1), pts.getLon(i + 1)
            );
            this.cum[i + 1] = this.cum[i] + segmentDist;
        }

        this.total = this.cum[size - 1];

        if (size > 0) {
            this.lat = pts.getLat(0);
            this.lon = pts.getLon(0);
            this.hasPosition = true;
        }
    }
}