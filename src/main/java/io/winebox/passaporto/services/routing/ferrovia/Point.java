package io.winebox.passaporto.services.routing.ferrovia;

import com.graphhopper.util.shapes.GHPoint;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by AJ on 7/24/16.
 */
@AllArgsConstructor
public final class Point {
    @Getter
    private final double latitude;

    @Getter
    private final double longitude;

    public GHPoint toGHPoint() {
        return new GHPoint(latitude, longitude);
    }

    @Override
    public String toString() {
        return latitude + "," + longitude;
    }
}