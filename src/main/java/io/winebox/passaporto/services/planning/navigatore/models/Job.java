package io.winebox.passaporto.services.planning.navigatore.models;

/**
 * Created by AJ on 7/28/16.
 */
public class Job {
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

    Job( String id, String name, int priority ) {
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