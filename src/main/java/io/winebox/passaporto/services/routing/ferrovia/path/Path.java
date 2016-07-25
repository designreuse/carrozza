package io.winebox.passaporto.services.routing.ferrovia.path;

import io.winebox.passaporto.services.routing.ferrovia.Point;
import lombok.Getter;

import java.util.List;

/**
 * Created by AJ on 7/24/16.
 */
public final class Path {
    @Getter
    private final double time;

    @Getter
    private final double distance;

    @Getter
    private final List<Leg> legs;

    public static final class Leg {
        @Getter
        final double time;

        @Getter
        final double distance;

        @Getter
        final List<Point> points;

        @Getter
        final String text;

        public Leg( double time, double distance, List<Point> points, String text ) {
            this.time = time;
            this.distance = distance;
            this.points = points;
            this.text = text;
        }

        @Override
        public String toString() {
            return "time: " + (int)time + " seconds, distance: " + (int)distance + " meters, points: " + points + " text: " + text;
        }
    }

    public Path( double time, double distance, List<Leg> legs ) {
        this.time = time;
        this.distance = distance;
        this.legs = legs;
    }

    @Override
    public String toString() {
        return "time: " + (int)time + " seconds, distance: " + (int)distance + " meters, legs: " + legs;
    }
}