package io.winebox.passaporto.services.planning.navigatore.models;

import com.graphhopper.jsprit.core.problem.Location;

/**
 * Created by AJ on 7/28/16.
 */
public final class Shipment extends Job {
    private final Stop pickup;
    private final Stop delivery;

    public Stop pickup() {
        return this.pickup;
    }

    public Stop delivery() {
        return this.delivery;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String name;
        private Priority priority;
        private Stop pickup;
        private Stop delivery;

        public Builder id( String id ) {
            this.id = id;
            return this;
        }

        public Builder name( String name ) {
            this.name = name;
            return this;
        }

        public Builder priority( Priority priority ) {
            this.priority = priority;
            return this;
        }

        public Builder pickup( Stop pickup ) {
            this.pickup = pickup;
            return this;
        }

        public Builder delivery( Stop delivery ) {
            this.delivery = delivery;
            return this;
        }

        public Shipment build() {
            return new Shipment(id, name, priority, pickup, delivery);
        }
    }

    @Override
    public com.graphhopper.jsprit.core.problem.job.Shipment toJsprit() {
        com.graphhopper.jsprit.core.problem.job.Shipment.Builder serviceBuilder = com.graphhopper.jsprit.core.problem.job.Shipment.Builder.newInstance(id())
                .setPickupLocation(Location.Builder.newInstance().setCoordinate(pickup().coordinate().toJsprit()).build())
                .setDeliveryLocation(Location.Builder.newInstance().setCoordinate(delivery().coordinate().toJsprit()).build());
        if (name() != null) serviceBuilder.setName(name());
        if (priority() != null) {
            switch (priority()) {
                case HIGH: serviceBuilder.setPriority(1); break;
                case MEDIUM: serviceBuilder.setPriority(2); break;
                case LOW: serviceBuilder.setPriority(3); break;
            }
        }
        if (pickup().timeWindows() != null && !pickup().timeWindows().isEmpty()) pickup().timeWindows().forEach((timeWindow) -> serviceBuilder.addPickupTimeWindow(timeWindow.toJsprit()));
        if (delivery().timeWindows() != null && !delivery().timeWindows().isEmpty()) delivery().timeWindows().forEach((timeWindow) -> serviceBuilder.addDeliveryTimeWindow(timeWindow.toJsprit()));
        return serviceBuilder.build();
    }

    Shipment(String id, String name, Priority priority, Stop pickup, Stop delivery ) {
        super(id, name, priority);
        this.pickup = pickup;
        this.delivery = delivery;
    }

    @Override
    public String toString() {
        return String.format("Shipment(id=%s, name=%s, priority=%s, pickup=%s, delivery=%s)", id(), name(), priority(), pickup(), delivery());
    }
}