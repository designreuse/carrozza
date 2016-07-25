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
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by AJ on 7/24/16.
 */
public final class Ferrovia {
    private final GraphHopper graphHopper;
    @Getter private String osmFile;
    @Getter private String graphLocation;
    @Getter private String flagEncoders;
    @Getter private String weightings;
    @Getter private String locale;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
//    private final RoadDataManager roadDataManager;

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String osmFile;
        private String graphLocation;
        private String flagEncoders;
        private String weightings;
//        private List<RoadDataUpdater> roadDataUpdaters = new ArrayList();

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

//        public Builder addRoadDataUpdater( RoadDataUpdater updater ) {
//            this.roadDataUpdaters.add(updater);
//            return this;
//        }

        public Ferrovia build() {
            return new Ferrovia(osmFile, graphLocation, flagEncoders, weightings/*, roadDataUpdaters*/);
        }
    }

    public Path path( PathRequest pathRequest ) throws PathException {
        return Path.doWork(graphHopper, pathRequest);
    }

    private Ferrovia( String osmFile, String graphLocation, String flagEncoders, String weightings/*s, List<RoadDataUpdater> roadDataUpdaters */) {
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

//        this.roadDataManager = new RoadDataManager(graphHopper, lock.writeLock(), roadDataUpdaters);
//        this.roadDataManager.start();
    }
}
