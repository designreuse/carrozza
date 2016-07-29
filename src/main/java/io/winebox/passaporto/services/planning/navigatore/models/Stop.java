package io.winebox.passaporto.services.planning.navigatore.models;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by AJ on 7/27/16.
 */
public final class Stop {
    private final Coordinate coordinate;
    private final Collection<TimeWindow> timeWindows;

    public Coordinate coordinate() {
        return this.coordinate;
    }

    public Collection<TimeWindow> timeWindows() {
        return this.timeWindows;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Coordinate coordinate;
        private Collection<TimeWindow> timeWindows;

        public Builder coordinate( Coordinate coordinate ) {
            this.coordinate = coordinate;
            return this;
        }

        public Builder timeWindow( TimeWindow timeWindow ) {
            if (this.timeWindows == null) {
                this.timeWindows = new ArrayList();
            }
            this.timeWindows.add(timeWindow);
            return this;
        }

        public Builder timeWindows( Collection<TimeWindow> timeWindows ) {
            if (this.timeWindows == null) {
                this.timeWindows = new ArrayList();
            }
            this.timeWindows.addAll(timeWindows);
            return this;
        }

        public Stop build() {
            return new Stop(coordinate, timeWindows);
        }
    }

    private Stop( Coordinate coordinate, Collection<TimeWindow> timeWindows ) {
        this.coordinate = coordinate;
        this.timeWindows = timeWindows;
    }

    @Override
    public String toString() {
        return String.format("Stop(coordinate=%s, timeWindows=%s)", coordinate(), timeWindows());
    }
}
