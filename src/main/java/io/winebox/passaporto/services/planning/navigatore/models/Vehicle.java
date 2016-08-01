package io.winebox.passaporto.services.planning.navigatore.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;

/**
 * Created by AJ on 7/31/16.
 */
public final class Vehicle {
    @JsonProperty("id")
    private final String id;
    @JsonProperty("type")
    private final String typeId;
    @JsonProperty("start")
    private final Coordinate start;

    public String id() {
        return this.id;
    }

    public String typeId() {
        return this.typeId;
    }

    public Coordinate start() {
        return this.start;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String typeId;
        private Coordinate start;

        public Builder id( String id ) {
            this.id = id;
            return this;
        }

        public Builder typeId( String typeId ) {
            this.typeId = typeId;
            return this;
        }

        public Builder start( Coordinate start ) {
            this.start = start;
            return this;
        }

        public Vehicle build() {
            return new Vehicle(id, typeId, start);
        }
    }

    public VehicleImpl toJsprit( VehicleTypeImpl vehicleType ) {
        return VehicleImpl.Builder.newInstance(id)
                .setType(vehicleType)
                .setReturnToDepot(false)
                .setStartLocation(Location.Builder.newInstance()
                        .setCoordinate(com.graphhopper.jsprit.core.util.Coordinate.newInstance(start().latitude(), start().longitude()))
                        .build()
                )
                .build();
    }

    public Vehicle( @JsonProperty("id") String id, @JsonProperty("type") String typeId, @JsonProperty("start") Coordinate start ) {
        this.id = id;
        this.typeId = typeId;
        this.start = start;
    }

    @Override
    public String toString() {
        return String.format("Vehicle(id=%s, type=%s, start=%s)", id(), typeId(), start());
    }
}
