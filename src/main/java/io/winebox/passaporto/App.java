package io.winebox.passaporto;

import com.graphhopper.util.CmdArgs;
import io.winebox.passaporto.services.routing.Ferrovia;

/**
 * Created by AJ on 7/24/16.
 */
public final class App {
    public static final void main( String[] args ) {
        CmdArgs graphHopperArgs = new CmdArgs()
                .put("osmreader.osm", "input/NewYork.osm")
                .put("graph.location", "output/graph-cache")
                .put("prepare.ch.weightings", "no")
                .put("graph.flag_encoders", "car|turn_costs=true");

        Ferrovia ferrovia = new Ferrovia(graphHopperArgs);
    }
}
