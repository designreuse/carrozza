package io.winebox.passaporto.services.routing.ferrovia;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Translation;
import com.graphhopper.util.shapes.GHPoint;
import gnu.trove.set.hash.TIntHashSet;
import io.winebox.passaporto.services.routing.ferrovia.path.Path;
import io.winebox.passaporto.services.routing.ferrovia.path.PathEdge;
import io.winebox.passaporto.services.routing.ferrovia.path.PathException;
import io.winebox.passaporto.services.routing.ferrovia.path.PathRequest;
import io.winebox.passaporto.services.routing.ferrovia.traffic.RoadData;
import io.winebox.passaporto.services.routing.ferrovia.traffic.RoadDataEntry;
import io.winebox.passaporto.services.routing.ferrovia.util.Point;
import lombok.Getter;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Created by AJ on 7/24/16.
 */
public final class Ferrovia {
    protected final GraphHopper graphHopper;
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Getter private String osmFile;
    @Getter private String graphLocation;
    @Getter private String flagEncoders;
    @Getter private String weightings;
    @Getter private String locale;

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String osmFile;
        private String graphLocation;
        private String flagEncoders;
        private String weightings;

        public Builder osmFile( @NonNull String osmFile ) {
            this.osmFile = osmFile;
            return this;
        }

        public Builder graphLocation( @NonNull String graphLocation ) {
            this.graphLocation = graphLocation;
            return this;
        }

        public Builder flagEncoders( @NonNull String flagEncoders ) {
            this.flagEncoders = flagEncoders;
            return this;
        }

        public Builder weightings( @NonNull String weightings ) {
            this.weightings = weightings;
            return this;
        }

        public Ferrovia build() {
            return new Ferrovia(osmFile, graphLocation, flagEncoders, weightings);
        }
    }

    public Path path( PathRequest pathRequest ) throws PathException {
        final List<GHPoint> graphHopperPoints = pathRequest.points().stream().map(Point::toGHPoint).collect(Collectors.toList());
        final GHRequest request = new GHRequest(graphHopperPoints);
        if (pathRequest.getEdges()) {
            request.getHints().put("calcPoints", pathRequest.calculatePoints());
            request.getHints().put("instructions", true);
        } else {
            request.getHints().put("calcPoints", false);
            request.getHints().put("instructions", false);
        }

        final GHResponse response = graphHopper.route(request);
        if (response.hasErrors()) {
            throw new PathException(response.getErrors());
        }
        final PathWrapper bestPath = response.getBest();
        final List<PathEdge> pathEdges;
        if (pathRequest.getEdges()) {
            final Translation translation = pathRequest.translateInstructions() ? graphHopper.getTranslationMap().getWithFallBack(new Locale(pathRequest.locale())) : null;
            pathEdges = new ArrayList();
            bestPath.getInstructions().forEach((instruction) -> {
                final double pathEdgeTime = Math.round(instruction.getTime() / 1000.);
                final double pathEdgeDistance = Math.round(instruction.getDistance());
                final List<Point> pathEdgePoints;
                if (pathRequest.calculatePoints()) {
                    pathEdgePoints = new ArrayList();
                    instruction.getPoints().forEach((point) -> {
                        final double latitude = new BigDecimal(point.getLat()).setScale(5, RoundingMode.HALF_UP).doubleValue();
                        final double longitude = new BigDecimal(point.getLon()).setScale(5, RoundingMode.HALF_UP).doubleValue();
                        final Point pathEdgePoint = Point.builder().latitude(latitude).longitude(longitude).build();
                        pathEdgePoints.add(pathEdgePoint);
                    });
                } else {
                    pathEdgePoints = null;
                }
                final String pathEdgeText = pathRequest.translateInstructions() ? instruction.getTurnDescription(translation) : null;
                final PathEdge pathEdge = new PathEdge(pathEdgeTime, pathEdgeDistance, pathEdgePoints, pathEdgeText);
                pathEdges.add(pathEdge);
            });
        } else {
            pathEdges = null;
        }
        final double pathTime = Math.round(bestPath.getTime() / 1000.);
        final double pathDistance = Math.round(bestPath.getDistance());
        final Path path = new Path(pathTime, pathDistance, pathEdges);
        return path;
    }

    public void feed( RoadData data ) {
        lock.writeLock().lock();
        try {
            lockedFeed(data);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void lockedFeed( RoadData data ) {
        Graph graph = graphHopper.getGraphHopperStorage().getBaseGraph();
        FlagEncoder carEncoder = graphHopper.getEncodingManager().getEncoder("car");
        LocationIndex locationIndex = graphHopper.getLocationIndex();

        int errors = 0;
        int updates = 0;
        TIntHashSet edgeIds = new TIntHashSet(data.size());
        System.out.println(data.size());
        for (RoadDataEntry entry : data) {

            // TODO get more than one point -> our map matching component
            Point point = entry.points().get(entry.points().size() / 2);
            QueryResult qr = locationIndex.findClosest(point.latitude(), point.longitude(), EdgeFilter.ALL_EDGES);
            if (!qr.isValid()) {
                 System.out.println("no matching road found for entry at " + point);
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
            double value = entry.speed();

            double oldSpeed = carEncoder.getSpeed(edge.getFlags());
            if (oldSpeed != value) {
                updates++;
                // TODO use different speed for the different directions (see e.g. Bike2WeightFlagEncoder)
                System.out.println("Speed change at (" + point + "). Old: " + oldSpeed + ", new:" + value);
                edge.setFlags(carEncoder.setSpeed(edge.getFlags(), value));
            }
        }
    }

    private Ferrovia( String osmFile, String graphLocation, String flagEncoders, String weightings) {
        final CmdArgs args = new CmdArgs()
                .put("osmreader.osm", osmFile)
                .put("graph.location", graphLocation)
                .put("graph.flag_encoders", flagEncoders)
                .put("prepare.ch.weightings", weightings);

        this.graphHopper = new GraphHopper() {
            @Override
            public GHResponse route(GHRequest request) {
                lock.readLock().lock();
                try {
                    return super.route(request);
                } finally {
                    lock.readLock().unlock();
                }
            }
        }
                .init(args)
                .forServer()
                .importOrLoad();
    }
}
