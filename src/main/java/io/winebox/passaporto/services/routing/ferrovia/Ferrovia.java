package io.winebox.passaporto.services.routing.ferrovia;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.Translation;
import com.graphhopper.util.shapes.GHPoint;
import io.winebox.passaporto.services.routing.ferrovia.path.PathDisplay;
import io.winebox.passaporto.services.routing.ferrovia.path.Path;
import io.winebox.passaporto.services.routing.ferrovia.path.PathException;
import io.winebox.passaporto.services.routing.ferrovia.path.PathRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Created by AJ on 7/24/16.
 */
public final class Ferrovia {

    private GraphHopper graphHopper;

    public static final class Builder {
        private String osmFilename = "";
        private String graphLocationFilename = "";
        private String flagEncoders = "car";
        private String chWeightings = "fastest";

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

        public Ferrovia build() {
            return new Ferrovia(osmFilename, graphLocationFilename, flagEncoders, chWeightings);
        }

        public static Builder newInstance( String osmFilename, String graphLocationFilename ) {
            final Builder builder = new Builder();
            builder.osmFilename = osmFilename;
            builder.graphLocationFilename = graphLocationFilename;
            return builder;
        }
    }

    private Ferrovia( String osmFilename, String graphLocationFilename, String flagEncoders, String chWeightings ) {
        final CmdArgs args = new CmdArgs()
                .put("osmreader.osm", osmFilename)
                .put("graph.location", graphLocationFilename)
                .put("graph.flag_encoders", flagEncoders)
                .put("prepare.ch.weightings", chWeightings);

        this.graphHopper = new GraphHopper()
                .init(args)
                .forServer()
                .importOrLoad();
    }

    public Path path( PathRequest pathRequest ) throws PathException {
        final List<GHPoint> graphHopperPoints = pathRequest.getPoints().stream().map(Point::toGHPoint).collect(Collectors.toList());
        final GHRequest request = new GHRequest(graphHopperPoints);
        final Locale locale;
        if (pathRequest.getLocale() != null) {
            locale = new Locale(pathRequest.getLocale());
        } else {
            locale = request.getLocale();
        }
        request.setLocale(locale);
        request.getHints().put("elevation", false);
        switch (pathRequest.getDisplay()) {
            case MINIMAL: request.getHints()
                    .put("instructions", false)
                    .put("calcPoints", false);
                break;
            case TERSE: request.getHints()
                    .put("calcPoints", false);
                break;
            default: break;
        }

        final GHResponse response = graphHopper.route(request);
        if (response.hasErrors()) {
            throw new PathException(response.getErrors());
        }
        final PathWrapper bestPath = response.getBest();
        final List<Path.Leg> legs;
        if (pathRequest.getDisplay() != PathDisplay.MINIMAL) {
            legs = new ArrayList();
            final Translation translation;
            if (pathRequest.getDisplay() == PathDisplay.DEFAULT) {
                translation = graphHopper.getTranslationMap().getWithFallBack(locale);
            } else {
                translation = null;
            }
            for (final Instruction instruction : bestPath.getInstructions()) {
                final List<Point> legPoints = new ArrayList();
                for (final GHPoint point : instruction.getPoints()) {
                    final double latitude = new BigDecimal(point.getLat()).setScale(5, RoundingMode.HALF_UP).doubleValue();
                    final double longitude = new BigDecimal(point.getLon()).setScale(5, RoundingMode.HALF_UP).doubleValue();
                    final Point legPoint = new Point(latitude, longitude);
                    legPoints.add(legPoint);
                }
                final String legText;
                if (pathRequest.getDisplay() == PathDisplay.DEFAULT) {
                    legText = instruction.getTurnDescription(translation);
                } else {
                    legText = null;
                }
                final double legTime = Math.round(instruction.getTime() / 1000.);
                final double legDistance = Math.round(instruction.getDistance());
                final Path.Leg leg = new Path.Leg(legTime, legDistance, legPoints, legText);
                legs.add(leg);
            }
        } else {
            legs = null;
        }
        final double pathTime = Math.round(bestPath.getTime() / 1000.);
        final double pathDistance = Math.round(bestPath.getDistance());
        final Path path = new Path(pathTime, pathDistance, legs);
        return path;
    }
}
