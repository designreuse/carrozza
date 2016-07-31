package io.winebox.passaporto.services.routing.ferrovia.path;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("time")
    @Getter private final double time;
    @JsonProperty("distance")
    @Getter private final double distance;
    @JsonProperty("points")
    @Getter private final List<Point> points;
    @JsonProperty("text")
    @Getter private final String text;
}