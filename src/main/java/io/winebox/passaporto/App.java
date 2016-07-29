package io.winebox.passaporto;

import io.winebox.passaporto.services.planning.navigatore.Navigatore;
import io.winebox.passaporto.services.planning.navigatore.models.*;
import io.winebox.passaporto.services.routing.ferrovia.Ferrovia;
import io.winebox.passaporto.services.routing.ferrovia.path.Path;
import io.winebox.passaporto.services.routing.ferrovia.path.PathRequest;
import io.winebox.passaporto.services.routing.ferrovia.traffic.RoadData;
import io.winebox.passaporto.services.routing.ferrovia.traffic.RoadDataEntry;
import io.winebox.passaporto.services.routing.ferrovia.traffic.RoadDataUpdater;
import io.winebox.passaporto.services.routing.ferrovia.traffic.examples.NewYorkRoadDataSource;
import io.winebox.passaporto.services.routing.ferrovia.util.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
                .build();

        RoadDataUpdater newYorkUpdater = new RoadDataUpdater(ferrovia, new NewYorkRoadDataSource(), 10);
        newYorkUpdater.start();

        RoadData data = new RoadData();
        List<Point> _points = new ArrayList();
        _points.add(Point.builder().latitude(40.753269).longitude(-73.985298).build());
        data.add(new RoadDataEntry(0, _points));

        ferrovia.feed(data);
        TimeUnit.SECONDS.sleep(4);

        final Navigatore navigatore = Navigatore.builder()
                .ferrovia(ferrovia)
                .build();

        final Service service1 = Service.builder()
                .id("service_1")
                .stop(Stop.builder()
                        .coordinate(Coordinate.builder()
                                .latitude(40.752279)
                                .longitude(-73.993505)
                                .build()
                        )
                        .build()
                )
                .build();

        final Shipment shipment1 = Shipment.builder()
                .id("shipment_1")
                .pickup(Stop.builder()
                        .coordinate(Coordinate.builder()
                                .latitude(40.753269)
                                .longitude(-73.985298)
                                .build()
                        )
                        .build()
                )
                .delivery(Stop.builder()
                        .coordinate(Coordinate.builder()
                                .latitude(40.754092)
                                .longitude(-73.978377)
                                .build()
                        )
                        .timeWindow(TimeWindow.builder()
                                .start(0)
                                .end(100)
                                .build()
                        )
                        .build()
                )
                .build();
        List<Job> jobs = new ArrayList();
        jobs.add(service1);
        jobs.add(shipment1);

        try {
            Route route = navigatore.route(jobs);
            List<Point> points = new ArrayList();
            route.forEach((activity) -> {
                Coordinate coordinate = activity.coordinate();
                points.add(Point.builder().latitude(coordinate.latitude()).longitude(coordinate.longitude()).build());
            });
            PathRequest pathRequest = PathRequest.builder()
                    .points(points)
                    .getEdges(true)
                    .calculatePoints(true)
                    .translateInstructions("en")
                    .build();
            Path path = ferrovia.path(pathRequest);
            System.out.println("Path time: " + path.time() + " seconds");
            System.out.println("Path distance: " + path.distance() + " meters");
            path.edges().forEach((edge) -> {
                System.out.println("Edge time: " + edge.time() + " seconds");
                System.out.println("Edge distance: " + edge.distance() + " meters");
                System.out.println("Instruction: " + edge.text());
            });
        } catch (Exception e) {
            System.out.println(e);
        }

        newYorkUpdater.stop();
    }
}
