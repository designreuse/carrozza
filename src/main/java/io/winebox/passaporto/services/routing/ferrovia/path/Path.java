package io.winebox.passaporto.services.routing.ferrovia.path;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * Created by AJ on 7/24/16.
 */
@AllArgsConstructor @ToString
public final class Path {
    @JsonProperty("time")
    @Getter private final double time;
    @JsonProperty("distance")
    @Getter private final double distance;
    @JsonProperty("edges")
    @Getter private final List<PathEdge> edges;
}