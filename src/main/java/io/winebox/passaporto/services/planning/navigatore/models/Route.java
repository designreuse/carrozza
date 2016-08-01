package io.winebox.passaporto.services.planning.navigatore.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AJ on 7/28/16.
 */
public final class Route {
    @JsonProperty("vehicle_id")
    private final String vehicleId;
    @JsonProperty("activities")
    private final List<Activity> activities;

    public String vehicleId() {
        return this.vehicleId;
    }

    public List<Activity> activities() {
        return this.activities;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String vehicleId;
        private List<Activity> activities;

        public Builder vehicleId( String vehicleId ) {
            this.vehicleId = vehicleId;
            return this;
        }

        public Builder activities( List<Activity> activities ) {
            this.activities = activities;
            return this;
        }

        public Route build() {
            return new Route(vehicleId, activities);
        }
    }

    public Route( @JsonProperty("vehicle_id") String vehicleId, @JsonProperty("activities") List<Activity> activities ) {
        this.vehicleId = vehicleId;
        this.activities = activities;
    }
}