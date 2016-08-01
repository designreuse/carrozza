package io.winebox.passaporto.services.planning.navigatore.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;

/**
 * Created by AJ on 7/31/16.
 */
public class VehicleType {
    @JsonProperty("id")
    private final String id;

    public String id() {
        return this.id;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;

        public Builder id( String id ) {
            this.id = id;
            return this;
        }

        public VehicleType build() {
            return new VehicleType(id);
        }
    }

    public VehicleTypeImpl toJsprit() {
        return VehicleTypeImpl.Builder.newInstance(id)
                .build();
    }

    public VehicleType( @JsonProperty("id") String id ) {
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("VehicleType(id=%s)", id());
    }
}
