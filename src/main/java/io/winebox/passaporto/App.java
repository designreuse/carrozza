package io.winebox.passaporto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static spark.Spark.*;

/**
 * Created by AJ on 7/24/16.
 */
public final class App {

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class TourPayload {
        @Getter @Setter @JsonProperty("points")
        List<Point> points;

        TourPayload( @JsonProperty("points") List<Point> points ) {
            this.points = points;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class RoutePayload {
        @Getter @Setter @JsonProperty("vehicles")
        List<Vehicle> vehicles;

        @Getter @Setter @JsonProperty("vehicle_types")
        List<VehicleType> vehicleTypes;

        @Getter @Setter @JsonProperty("jobs")
        List<Job> jobs;

        RoutePayload( @JsonProperty("vehicles") List<Vehicle> vehicles, @JsonProperty("vehicle_types") List<VehicleType> vehicleTypes, @JsonProperty("jobs") List<Job> jobs ) {
            this.vehicles = vehicles;
            this.vehicleTypes = vehicleTypes;
            this.jobs = jobs;
        }
    }

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

        options("/*", (request, response) -> {

            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Method", "POST");
            response.header("Access-Control-Allow-Headers", "*");
            // Note: this may or may not be necessary in your particular application
            response.type("application/json");
        });

        post("/traffic", (request, response) -> {
            response.type("application/json");
            final ObjectMapper mapper = new ObjectMapper();
            final Coordinate coordinate;
            try {
                coordinate = mapper.readValue(request.body(), Coordinate.class);
            } catch (Exception e) {
                System.out.println(e);
                response.status(400);
                return "{\"code\": \"BX\"}";
            }
            try {
                RoadData trafficData = new RoadData();
                List<Point> trafficPoints = new ArrayList();
                trafficPoints.add(Point.builder().latitude(coordinate.latitude()).longitude(coordinate.longitude()).build());
                trafficData.add(new RoadDataEntry(0, trafficPoints));

                ferrovia.feed(trafficData);
                return "";
            } catch (Exception e) {
                return "{\"code\": \"UE\"}";
            }
        });

        post("/tour", (request, response) -> {
            System.out.println(request.body());
            response.type("application/json");
            final ObjectMapper mapper = new ObjectMapper();
            final TourPayload payload;
            try {
                payload = mapper.readValue(request.body(), TourPayload.class);
            } catch (Exception e) {
                System.out.println(e);
                response.status(400);
                return "{\"code\": \"BX\"}";
            }
            boolean optimize;
            try {
                optimize = request.queryMap().get("optimize").booleanValue();
            } catch (Exception e) {
                optimize = false;
            }
            List<Point> points;
            if (optimize) {
                List<Job> jobs = payload.points.stream().map((point) -> {
                    return Service.builder()
                            .id(UUID.randomUUID().toString())
                            .stop(Stop.builder()
                                    .coordinate(Coordinate.builder()
                                            .latitude(point.latitude())
                                            .longitude(point.longitude())
                                            .build()
                                    )
                                    .build()
                            )
                            .build();
                }).collect(Collectors.toList());

                try {
                    Route route = navigatore.route(jobs);
                    points = route.activities().stream()
                            .map((activity) -> {
                                Coordinate coordinate = activity.coordinate();
                                return Point.builder().latitude(coordinate.latitude()).longitude(coordinate.longitude()).build();
                            })
                            .collect(Collectors.toList());
                } catch (Exception e) {
                    System.out.println(e);
                    return "{\"code\": \"UE\"}";
                }
            } else {
                points = payload.points;
            }
            try {
                PathRequest pathRequest = PathRequest.builder()
                        .points(points)
                        .getEdges(true)
                        .calculatePoints(true)
                        .translateInstructions("en")
                        .build();
                Path path = ferrovia.path(pathRequest);
                return mapper.writeValueAsString(path);
            } catch (Exception e) {
                System.out.println(e);
                return "{\"code\": \"UE\"}";
            }
        });

        post("/plan", ((request, response) -> {
            response.type("application/json");
            final ObjectMapper mapper = new ObjectMapper();
            final RoutePayload payload;
            try {
                payload = mapper.readValue(request.body(), RoutePayload.class);
            } catch (Exception e) {
                System.out.println(e);
                response.status(400);
                return "{\"code\": \"BX\"}";
            }
            Plan plan;
            try {
                plan = navigatore.plan(payload.vehicles(), payload.vehicleTypes(), payload.jobs());
            } catch (Exception e) {
                System.out.println(e);
                return "{\"code\": \"UE\"}";
            }
            return mapper.writeValueAsString(plan);
        }));

//        final Service service1 = Service.builder()
//                .id("service_1")
//                .stop(Stop.builder()
//                        .coordinate(Coordinate.builder()
//                                .latitude(40.752279)
//                                .longitude(-73.993505)
//                                .build()
//                        )
//                        .build()
//                )
//                .build();
//
//        final Shipment shipment1 = Shipment.builder()
//                .id("shipment_1")
//                .pickup(Stop.builder()
//                        .coordinate(Coordinate.builder()
//                                .latitude(40.753269)
//                                .longitude(-73.985298)
//                                .build()
//                        )
//                        .build()
//                )
//                .delivery(Stop.builder()
//                        .coordinate(Coordinate.builder()
//                                .latitude(40.754092)
//                                .longitude(-73.978377)
//                                .build()
//                        )
////                        .timeWindow(TimeWindow.builder()
////                                .start(0)
////                                .end(100)
////                                .build()
////                        )
//                        .build()
//                )
//                .build();
//        List<Job> jobs = new ArrayList();
//        jobs.add(service1);
//        jobs.add(shipment1);
//
//        try {
//            Route route = navigatore.route(jobs);
//            List<Point> points = new ArrayList();
//            route.forEach((activity) -> {
//                System.out.println(activity);
//                Coordinate coordinate = activity.coordinate();
//                points.add(Point.builder().latitude(coordinate.latitude()).longitude(coordinate.longitude()).build());
//            });
//            PathRequest pathRequest = PathRequest.builder()
//                    .points(points)
//                    .getEdges(true)
//                    .calculatePoints(true)
//                    .translateInstructions("es")
//                    .build();
//            Path path = ferrovia.path(pathRequest);
//            System.out.println("Path time: " + path.time() + " seconds");
//            System.out.println("Path distance: " + path.distance() + " meters");
//            path.edges().forEach((edge) -> {
//                System.out.println("Edge time: " + edge.time() + " seconds");
//                System.out.println("Edge distance: " + edge.distance() + " meters");
//                System.out.println("Instruction: " + edge.text());
//            });
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//
//        newYorkUpdater.stop();
    }

}
