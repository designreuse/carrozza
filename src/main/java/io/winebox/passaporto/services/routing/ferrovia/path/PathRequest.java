package io.winebox.passaporto.services.routing.ferrovia.path;

import io.winebox.passaporto.services.routing.ferrovia.Point;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AJ on 7/24/16.
 */
public final class PathRequest {
    @Getter
    private List<Point> points;

    @Getter
    private PathDisplay display = PathDisplay.DEFAULT;

    @Getter
    private String locale;

    public static final class Builder {
        private List<Point> points;
        private PathDisplay display = PathDisplay.DEFAULT;
        private String locale;

        public Builder setPoints( List<Point> points ) {
            this.points = points;
            return this;
        }

        public Builder setDisplay( PathDisplay display ) {
            this.display = display;
            return this;
        }

        public Builder setLocale( String locale ) {
            this.locale = locale;
            return this;
        }

        public PathRequest build() {
            return new PathRequest( points, display, locale );
        }

        public static Builder newInstance( List<Point> points ) {
            final Builder builder = new Builder();
            builder.points = points;
            return builder;
        }

        public static Builder newInstance( Point from, Point to ) {
            final List<Point> points = new ArrayList();
            points.add(from);
            points.add(to);
            return newInstance(points);
        }
    }

    private PathRequest(List<Point> points, PathDisplay display, String locale ) {
        this.points = points;
        this.display = display;
        this.locale = locale;
    }
}