package io.winebox.passaporto.services.planning.navigatore.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by AJ on 7/28/16.
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Service.class, name = "service"),
        @JsonSubTypes.Type(value = Shipment.class, name = "shipment")
})
public abstract class Job {
    private final String id;
    private final String name;
    private final Integer priority;

    public String id() {
        return this.id;
    }

    public String name() {
        return this.name;
    }

    public Integer priority() {
        return this.priority;
    }

    Job( String id, String name, Integer priority ) {
        this.id = id;
        this.name = name;
        this.priority = priority;
    }

    public com.graphhopper.jsprit.core.problem.job.Job toJsprit() {
        return null;
    }

    @Override
    public String toString() {
        return String.format("Coordinate(id=%s, name=%s, priority=%s)", id(), name(), priority());
    }
}