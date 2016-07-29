package io.winebox.passaporto.services.routing.ferrovia.util;

import com.graphhopper.util.shapes.GHPoint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * Created by AJ on 7/24/16.
 */
//@AllArgsConstructor
//public final class Point {
//    @NonNull @Getter private final double latitude;
//    @Getter private final double longitude;
//    public GHPoint toGHPoint() {
//        return new GHPoint(latitude, longitude);
//    }
//
//    @Override
//    public String toString() {
//        return latitude + "," + longitude;
//    }
//}

public final class Point {
    private final double latitude;
    private final double longitude;

    public double latitude() {
        return this.latitude;
    }

    public double longitude() {
        return this.longitude;
    }

    public GHPoint toGHPoint() {
        return new GHPoint(latitude(), longitude());
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

        public Point build() {
            return new Point(latitude, longitude);
        }
    }

    private Point( double latitude, double longitude ) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return String.format("Point(latitude=%.5f, longitude=%.5f)", latitude(), longitude());
    }
}