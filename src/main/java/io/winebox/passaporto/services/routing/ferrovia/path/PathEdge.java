package io.winebox.passaporto.services.routing.ferrovia.path;

import io.winebox.passaporto.services.routing.ferrovia.util.Point;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * Created by AJ on 7/25/16.
 */
@AllArgsConstructor @ToString
public final class PathEdge {
    @Getter private final double time;
    @Getter private final double distance;
    @Getter private final List<Point> points;
    @Getter private final String text;
}