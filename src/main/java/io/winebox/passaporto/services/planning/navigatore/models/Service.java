package io.winebox.passaporto.services.planning.navigatore.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.jsprit.core.problem.Location;

/**
 * Created by AJ on 7/28/16.
 */
public final class Service extends Job {
    @JsonProperty("stop")
    private final Stop stop;

    public Stop stop() {
        return this.stop;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String name;
        private Integer priority;
        private Stop stop;

        public Builder id( String id ) {
            this.id = id;
            return this;
        }

        public Builder name( String name ) {
            this.name = name;
            return this;
        }

        public Builder priority( Integer priority ) {
            this.priority = priority;
            return this;
        }

        public Builder stop( Stop stop ) {
            this.stop = stop;
            return this;
        }

        public Service build() {
            return new Service(id, name, priority, stop);
        }
    }

    @Override
    public com.graphhopper.jsprit.core.problem.job.Service toJsprit() {
        com.graphhopper.jsprit.core.problem.job.Service.Builder serviceBuilder = com.graphhopper.jsprit.core.problem.job.Service.Builder.newInstance(id())
                .setLocation(Location.Builder.newInstance().setCoordinate(stop().coordinate().toJsprit()).build());
        if (name() != null) serviceBuilder.setName(name());
        if (priority() != null) {
            serviceBuilder.setPriority(priority());
        }
        if (stop().timeWindows() != null && !stop().timeWindows().isEmpty()) stop().timeWindows().forEach((timeWindow) -> serviceBuilder.addTimeWindow(timeWindow.start(), timeWindow.end()));
        return serviceBuilder.build();
    }

    public Service( @JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("priority") Integer priority, @JsonProperty("stop") Stop stop ) {
        super(id, name, priority);
        this.stop = stop;
    }

    @Override
    public String toString() {
        return String.format("Service(id=%s, name=%s, priority=%s, stop=%s)", id(), name(), priority(), stop());
    }
}
