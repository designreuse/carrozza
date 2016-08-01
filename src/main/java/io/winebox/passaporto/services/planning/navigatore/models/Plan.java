package io.winebox.passaporto.services.planning.navigatore.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by AJ on 7/31/16.
 */
public class Plan {
    @JsonProperty("unassigned_jobs")
    private final List<String> unassignedJobs;
    @JsonProperty("routes")
    private final List<Route> routes;

    public List<String> unassignedJobs() {
        return this.unassignedJobs;
    }

    public List<Route> routes() {
        return this.routes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private List<String> unassignedJobs;
        private List<Route> routes;

        public Builder unassignedJobs( List<String> unassignedJobs ) {
            this.unassignedJobs = unassignedJobs;
            return this;
        }

        public Builder routes(List<Route> routes ) {
            this.routes = routes;
            return this;
        }

        public Plan build() {
            return new Plan(unassignedJobs, routes);
        }
    }

    public Plan( @JsonProperty("unassigned_jobs") List<String> unassignedJobs, @JsonProperty("routes") List<Route> routes ) {
        this.unassignedJobs = unassignedJobs;
        this.routes = routes;
    }
}
