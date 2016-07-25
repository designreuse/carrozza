package io.winebox.passaporto.services.routing;

import com.graphhopper.GraphHopper;
import com.graphhopper.util.CmdArgs;

import java.util.List;

/**
 * Created by AJ on 7/24/16.
 */
public final class Ferrovia {

    private GraphHopper graphHopper;

    public static final class Point {
        final double latitude;
        final double longitude;

        public Point( double latitude, double longitude ) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public Ferrovia( CmdArgs args ) {
        this.graphHopper = new GraphHopper()
                .init(args)
                .forServer()
                .importOrLoad();
    }
}
