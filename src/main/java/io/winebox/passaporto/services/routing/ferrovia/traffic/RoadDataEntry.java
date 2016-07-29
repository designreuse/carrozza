package io.winebox.passaporto.services.routing.ferrovia.traffic;

import io.winebox.passaporto.services.routing.ferrovia.util.Point;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * Created by AJ on 7/25/16.
 */
@AllArgsConstructor @ToString
public class RoadDataEntry {
    @Getter private final double speed;
    @Getter private final List<Point> points;
}
