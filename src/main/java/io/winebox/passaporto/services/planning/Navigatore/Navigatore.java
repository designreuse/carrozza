package io.winebox.passaporto.services.planning.navigatore;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.SchrimpfFactory;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.*;
import com.graphhopper.util.shapes.GHPoint;
import io.winebox.passaporto.services.planning.navigatore.models.*;
import io.winebox.passaporto.services.planning.navigatore.models.Coordinate;
import io.winebox.passaporto.services.routing.ferrovia.Ferrovia;
import io.winebox.passaporto.services.routing.ferrovia.path.Path;
import io.winebox.passaporto.services.routing.ferrovia.path.PathRequest;
import io.winebox.passaporto.services.routing.ferrovia.util.Point;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by AJ on 7/27/16.
 */
public final class Navigatore {
    private final Ferrovia ferrovia;

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Ferrovia ferrovia;

        public Builder ferrovia( Ferrovia ferrovia ) {
            this.ferrovia = ferrovia;
            return this;
        }

        public Navigatore build() {
            return new Navigatore(ferrovia);
        }
    }

    public Route route( Collection<Job> jobs ) {
        final List<Location> locations = new ArrayList();
        final Location dummyLocation = Location.Builder
                .newInstance()
                .setId("origin")
                .setCoordinate(com.graphhopper.jsprit.core.util.Coordinate.newInstance(0, 0))
                .build();
        final Vehicle vehicle = VehicleImpl.Builder
                .newInstance("no_vehicle")
                .setStartLocation(dummyLocation)
                .setReturnToDepot(false)
                .build();
        locations.add(dummyLocation);

        final Collection<com.graphhopper.jsprit.core.problem.job.Job> _jobs = jobs.stream()
                .map((job) -> job.toJsprit())
                .collect(Collectors.toList());
        jobs.forEach((job) -> {
            if (job instanceof Service) {
                final Service service = (Service)job;
                locations.add(Location.Builder.newInstance().setCoordinate(service.stop().coordinate().toJsprit()).build());
            }
            if (job instanceof Shipment) {
                final Shipment shipment = (Shipment)job;
                locations.add(Location.Builder.newInstance().setCoordinate(shipment.pickup().coordinate().toJsprit()).build());
                locations.add(Location.Builder.newInstance().setCoordinate(shipment.delivery().coordinate().toJsprit()).build());
            }
        });

        final VehicleRoutingTransportCostsMatrix.Builder costsMatrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        for (final Location fromLocation : locations) {
            for (final Location toLocation : locations) {
                if (fromLocation.getId().equals("origin")) {
                    costsMatrixBuilder.addTransportDistance(fromLocation.getId(), toLocation.getId(), 0);
                    costsMatrixBuilder.addTransportTime(fromLocation.getId(), toLocation.getId(), 0);
                    continue;
                }
                if (toLocation.getId().equals("origin")) {
                    costsMatrixBuilder.addTransportDistance(fromLocation.getId(), toLocation.getId(), Double.POSITIVE_INFINITY);
                    costsMatrixBuilder.addTransportTime(fromLocation.getId(), toLocation.getId(), Double.POSITIVE_INFINITY);
                    continue;
                }
                if (fromLocation.equals(toLocation)) {
                    costsMatrixBuilder.addTransportDistance(fromLocation.getId(), toLocation.getId(), 0);
                    costsMatrixBuilder.addTransportTime(fromLocation.getId(), toLocation.getId(), 0);
                    continue;
                }
                PathRequest pathRequest = PathRequest.builder()
                        .point(Point.builder()
                                .latitude(fromLocation.getCoordinate().getX())
                                .longitude(fromLocation.getCoordinate().getY())
                                .build()
                        )
                        .point(Point.builder()
                                .latitude(toLocation.getCoordinate().getX())
                                .longitude(toLocation.getCoordinate().getY())
                                .build())
                        .getEdges(false)
                        .build();
                Path path;
                try {
                    path = ferrovia.path(pathRequest);
                } catch (Exception e) {
                    System.out.println(e);
                    path = null;
                }
                costsMatrixBuilder.addTransportDistance(fromLocation.getId(), toLocation.getId(), path.distance());
                costsMatrixBuilder.addTransportTime(fromLocation.getId(), toLocation.getId(), path.time());
            }
        }

        final VehicleRoutingTransportCostsMatrix costsMatrix = costsMatrixBuilder.build();
        final VehicleRoutingProblem vehicleRoutingProblem = VehicleRoutingProblem.Builder.newInstance()
                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
                .setRoutingCost(costsMatrix)
                .addAllJobs(_jobs)
                .addVehicle(vehicle)
                .build();

        final Route route = new Route();
        final VehicleRoutingAlgorithm algorithm = new SchrimpfFactory().createAlgorithm(vehicleRoutingProblem);
        final Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        final VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);
        for (final VehicleRoute _route : solution.getRoutes()) {
//            route.add(Activity.builder()
//                    .id(null)
//                    .name(_route.getStart().getName())
//                    .coordinate(Coordinate.builder()
//                            .latitude(_route.getStart().getLocation().getCoordinate().getX())
//                            .longitude(_route.getStart().getLocation().getCoordinate().getY())
//                            .build()
//                    )
//                    .timeWindow(TimeWindow.builder()
//                            .start(-1)
//                            .end(Math.round(_route.getStart().getEndTime()))
//                            .build()
//                    )
//                    .build()
//            );
            for (final TourActivity activity : _route.getActivities()) {
                final String jobId;
                if (activity instanceof TourActivity.JobActivity) {
                    jobId = ((TourActivity.JobActivity) activity).getJob().getId();
                } else {
                    jobId = null;
                }
                final String activityName;
                switch (activity.getName()) {
                    case "pickupShipment": activityName = "pickup"; break;
                    case "deliverShipment": activityName = "dropoff"; break;
                    default: activityName = activity.getName(); break;
                }
                route.add(Activity.builder()
                        .id(jobId)
                        .name(activityName)
                        .coordinate(Coordinate.builder()
                                .latitude(activity.getLocation().getCoordinate().getX())
                                .longitude(activity.getLocation().getCoordinate().getY())
                                .build()
                        )
                        .timeWindow(TimeWindow.builder()
                                .start(Math.round(activity.getArrTime()))
                                .end(Math.round(activity.getEndTime()))
                                .build()
                        )
                        .build()
                );
            }
//            route.add(Activity.builder()
//                    .id(null)
//                    .name(_route.getEnd().getName())
//                    .coordinate(Coordinate.builder()
//                            .latitude(_route.getEnd().getLocation().getCoordinate().getX())
//                            .longitude(_route.getEnd().getLocation().getCoordinate().getY())
//                            .build()
//                    )
//                    .timeWindow(TimeWindow.builder()
//                            .start(Math.round(_route.getEnd().getArrTime()))
//                            .end(-1)
//                            .build()
//                    )
//                    .build()
//            );
        }
        return route;
    }

    private Navigatore( Ferrovia ferrovia ) {
        this.ferrovia = ferrovia;
    }
}
