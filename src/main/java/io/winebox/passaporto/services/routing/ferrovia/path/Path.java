package io.winebox.passaporto.services.routing.ferrovia.path;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.Translation;
import com.graphhopper.util.shapes.GHPoint;
import io.winebox.passaporto.services.routing.ferrovia.Point;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Created by AJ on 7/24/16.
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED) @ToString
public final class Path {
    @Getter private final double time;
    @Getter private final double distance;
    @Getter private final List<PathEdge> edges;

    public static Path doWork( GraphHopper graphHopper, PathRequest pathRequest ) throws PathException {
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
                        final Point pathEdgePoint = new Point(latitude, longitude);
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
}