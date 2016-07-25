package io.winebox.passaporto;

import io.winebox.passaporto.services.routing.ferrovia.Ferrovia;
import io.winebox.passaporto.services.routing.ferrovia.Point;
import io.winebox.passaporto.services.routing.ferrovia.path.Path;
import io.winebox.passaporto.services.routing.ferrovia.path.PathRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AJ on 7/24/16.
 */
public final class App {
    public static final void main( String[] args ) throws Exception {
        final Ferrovia ferrovia = Ferrovia.builder()
                .osmFile("input/NewYork.osm")
                .graphLocation("output/graph-cache")
                .flagEncoders("car|turn_costs=true")
                .weightings("no")
//                .addRoadDataUpdater(RoadDataUpdater.Builder.newInstance(new NewYorkRoadDataSource()).setRefreshRate(30).build())
                .build();
//
//        Thread.sleep(2 * 1000);
//
        final List<Point> points = new ArrayList();
        points.add(new Point(40.752279, -73.993505));
        points.add(new Point(40.754092, -73.978377));
        final PathRequest pathRequest = PathRequest.builder()
                .points(points)
                .build();
        try {
            final Path path = ferrovia.path(pathRequest);
            System.out.println(path);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
