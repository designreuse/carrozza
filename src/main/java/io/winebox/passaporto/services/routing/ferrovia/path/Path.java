package io.winebox.passaporto.services.routing.ferrovia.path;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * Created by AJ on 7/24/16.
 */
@AllArgsConstructor @ToString
public final class Path {
    @Getter private final double time;
    @Getter private final double distance;
    @Getter private final List<PathEdge> edges;
}