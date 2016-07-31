package io.winebox.passaporto.services.planning.navigatore.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AJ on 7/28/16.
 */
public final class Activity {
    @JsonProperty("id")
    private final String id;
    @JsonProperty("name")
    private final String name;
    @JsonProperty("coordinate")
    private final Coordinate coordinate;
    @JsonProperty("time_window")
    private final TimeWindow timeWindow;

    public String id() {
        return this.id;
    }

    public String name() {
        return this.name;
    }

    public Coordinate coordinate() {
        return this.coordinate;
    }

    public TimeWindow timeWindow() {
        return this.timeWindow;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String name;
        private Coordinate coordinate;
        private TimeWindow timeWindow;

        public Builder id( String id ) {
            this.id = id;
            return this;
        }

        public Builder name( String name ) {
            this.name = name;
            return this;
        }

        public Builder coordinate( Coordinate coordinate ) {
            this.coordinate = coordinate;
            return this;
        }

        public Builder timeWindow( TimeWindow timeWindow ) {
            this.timeWindow = timeWindow;
            return this;
        }

        public Activity build() {
            return new Activity(id, name, coordinate, timeWindow);
        }
    }

    public Activity( @JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("coordinate") Coordinate coordinate, @JsonProperty("time_window") TimeWindow timeWindow ) {
        this.id = id;
        this.name = name;
        this.coordinate = coordinate;
        this.timeWindow = timeWindow;
    }

    @Override
    public String toString() {
        return String.format("Activity(id=%s, name=%s, coordinate=%s, timeWindow=%s)", id(), name(), coordinate(), timeWindow());
    }
}
