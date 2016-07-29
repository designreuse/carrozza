package io.winebox.passaporto.services.routing.ferrovia.path;

import io.winebox.passaporto.services.routing.ferrovia.util.Point;
import lombok.*;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by AJ on 7/24/16.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE) @ToString
public final class PathRequest {
    @Getter private Collection<Point> points;
    @Getter private boolean getEdges;
    @Getter private boolean calculatePoints;
    @Getter private boolean translateInstructions;
    @Getter private String locale;

    public static Builder builder() {
        return new Builder();
    }

    @ToString
    public static final class Builder {
        @Getter(value = AccessLevel.PRIVATE, lazy = true) private final Collection<Point> points = new ArrayList();
        private boolean getEdges = false;
        private boolean calculatePoints = false;
        private boolean translateInstructions = false;
        private String locale;

        public Builder point( @NonNull Point point ) {
            this.points().add(point);
            return this;
        }

        public Builder points( @NonNull Collection<Point> points ) {
            this.points().addAll(points);
            return this;
        }

        public Builder getEdges( boolean getEdges ) {
            this.getEdges = getEdges;
            return this;
        }

        public Builder calculatePoints( boolean calculatePoints ) {
            this.calculatePoints = calculatePoints;
            return this;
        }

        public Builder translateInstructions( String locale ) {
            this.translateInstructions = locale != null;
            this.locale = locale;
            return this;
        }

        public PathRequest build() {
            return new PathRequest(this.points(), getEdges, calculatePoints, translateInstructions, locale);
        }
    }
}