package io.winebox.passaporto.services.planning.navigatore.models;

import com.graphhopper.jsprit.core.problem.Location;

/**
 * Created by AJ on 7/27/16.
 */
public final class Coordinate {
    private final double latitude;
    private final double longitude;

    public double latitude() {
        return this.latitude;
    }

    public double longitude() {
        return this.longitude;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private double latitude;
        private double longitude;

        public Builder latitude( double latitude ) {
            this.latitude = latitude;
            return this;
        }

        public Builder longitude( double longitude ) {
            this.longitude = longitude;
            return this;
        }

        public Coordinate build() {
            return new Coordinate(latitude, longitude);
        }
    }

    public com.graphhopper.jsprit.core.util.Coordinate toJsprit() {
        return com.graphhopper.jsprit.core.util.Coordinate.newInstance(latitude(), longitude());
    }

    private Coordinate( double latitude, double longitude ) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return String.format("Coordinate(latitude=%.5f, longitude=%.5f)", latitude(), longitude());
    }
}
