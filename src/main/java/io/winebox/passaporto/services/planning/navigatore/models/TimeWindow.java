package io.winebox.passaporto.services.planning.navigatore.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AJ on 7/27/16.
 */
public final class TimeWindow {
    @JsonProperty("start")
    private final double start;
    @JsonProperty("end")
    private final double end;

    public double start() {
        return this.start;
    }

    public double end() {
        return this.end;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private double start;
        private double end;

        public Builder start( double start ) {
            this.start = start;
            return this;
        }

        public Builder end( double end ) {
            this.end = end;
            return this;
        }

        public TimeWindow build() {
            return new TimeWindow(start, end);
        }
    }

    public com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow toJsprit() {
        return com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow.newInstance(start(), end());
    }

    public TimeWindow( @JsonProperty("start") double start, @JsonProperty("end") double end ) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return String.format("TimeWindow(start=%.5f, end=%.5f)", start(), end());
    }
}
