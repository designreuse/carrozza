package io.winebox.passaporto.services.routing.ferrovia.traffic;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.EdgeIteratorState;
import gnu.trove.set.hash.TIntHashSet;
import io.winebox.passaporto.services.routing.ferrovia.Point;

import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Created by AJ on 7/25/16.
 */
public class RoadDataManager {

    private GraphHopper graphHopper;
    private Lock writeLock;
    private List<RoadDataUpdater> updaters;

    public void start() {
        updaters.forEach((updater) -> updater.start(this));
    }

    protected void feed( RoadData data ) {
        writeLock.lock();
        try {
            lockedFeed(data);
        } finally {
            writeLock.unlock();
        }
    }

    private void lockedFeed( RoadData data ) {
//        currentRoads = data;
        Graph graph = graphHopper.getGraphHopperStorage().getBaseGraph();
        FlagEncoder carEncoder = graphHopper.getEncodingManager().getEncoder("car");
        LocationIndex locationIndex = graphHopper.getLocationIndex();

        int errors = 0;
        int updates = 0;
        TIntHashSet edgeIds = new TIntHashSet(data.size());
        System.out.println(data.size());
        for (RoadEntry entry : data) {

            // TODO get more than one point -> our map matching component
            Point point = entry.getPoints().get(entry.getPoints().size() / 2);
            QueryResult qr = locationIndex.findClosest(point.getLatitude(), point.getLongitude(), EdgeFilter.ALL_EDGES);
            if (!qr.isValid()) {
                // logger.info("no matching road found for entry " + entry.getId() + " at " + point);
                errors++;
                continue;
            }

            int edgeId = qr.getClosestEdge().getEdge();
            if (edgeIds.contains(edgeId)) {
                // TODO this wouldn't happen with our map matching component
                errors++;
                continue;
            }

            edgeIds.add(edgeId);
            EdgeIteratorState edge = graph.getEdgeIteratorState(edgeId, Integer.MIN_VALUE);
            double value = entry.getSpeed();

            double oldSpeed = carEncoder.getSpeed(edge.getFlags());
            if (oldSpeed != value) {
                updates++;
                // TODO use different speed for the different directions (see e.g. Bike2WeightFlagEncoder)
//                System.out.println("Speed change at " + entry.getId() + " (" + point + "). Old: " + oldSpeed + ", new:" + value);
                edge.setFlags(carEncoder.setSpeed(edge.getFlags(), value));
            }
        }

        System.out.println("Updated " + updates + " street elements of " + data.size() + ". Unchanged:" + (data.size() - updates) + ", errors:" + errors);
    }

    public void stop() {
        updaters.forEach(RoadDataUpdater::stop);
    }

    public RoadDataManager( GraphHopper graphHopper, Lock writeLock, List<RoadDataUpdater> updaters ) {
        this.graphHopper = graphHopper;
        this.writeLock = writeLock;
        this.updaters = updaters;
    }
}
