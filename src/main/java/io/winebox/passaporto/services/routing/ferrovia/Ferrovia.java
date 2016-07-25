package io.winebox.passaporto.services.routing.ferrovia;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.util.CmdArgs;
import io.winebox.passaporto.services.routing.ferrovia.path.Path;
import io.winebox.passaporto.services.routing.ferrovia.path.PathException;
import io.winebox.passaporto.services.routing.ferrovia.path.PathRequest;
import io.winebox.passaporto.services.routing.ferrovia.traffic.RoadDataManager;
import io.winebox.passaporto.services.routing.ferrovia.traffic.RoadDataUpdater;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by AJ on 7/24/16.
 */
public final class Ferrovia {

    private final GraphHopper graphHopper;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final RoadDataManager roadDataManager;

    public static final class Builder {
        private String osmFilename = "";
        private String graphLocationFilename = "";
        private String flagEncoders = "car";
        private String chWeightings = "fastest";
        private List<RoadDataUpdater> roadDataUpdaters = new ArrayList();

        public Builder setOSMFilename( String osmFilename ) {
            this.osmFilename = osmFilename;
            return this;
        }

        public Builder setGraphLocationFilename( String graphLocationFilename ) {
            this.graphLocationFilename = osmFilename;
            return this;
        }

        public Builder setFlagEncoders( String flagEncoders ) {
            this.flagEncoders = flagEncoders;
            return this;
        }

        public Builder setCHWeightings( String chWeightings ) {
            this.chWeightings = chWeightings;
            return this;
        }

        public Builder addRoadDataUpdater( RoadDataUpdater updater ) {
            this.roadDataUpdaters.add(updater);
            return this;
        }

        public Ferrovia build() {
            return new Ferrovia(osmFilename, graphLocationFilename, flagEncoders, chWeightings, roadDataUpdaters);
        }

        public static Builder newInstance( String osmFilename, String graphLocationFilename ) {
            final Builder builder = new Builder();
            builder.osmFilename = osmFilename;
            builder.graphLocationFilename = graphLocationFilename;
            return builder;
        }
    }

    public Path path( PathRequest pathRequest ) throws PathException {
        return Path.doWork(graphHopper, pathRequest);
    }

    private Ferrovia( String osmFilename, String graphLocationFilename, String flagEncoders, String chWeightings, List<RoadDataUpdater> roadDataUpdaters ) {
        final CmdArgs args = new CmdArgs()
                .put("osmreader.osm", osmFilename)
                .put("graph.location", graphLocationFilename)
                .put("graph.flag_encoders", flagEncoders)
                .put("prepare.ch.weightings", chWeightings);

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

        this.roadDataManager = new RoadDataManager(graphHopper, lock.writeLock(), roadDataUpdaters);
        this.roadDataManager.start();
    }
}
