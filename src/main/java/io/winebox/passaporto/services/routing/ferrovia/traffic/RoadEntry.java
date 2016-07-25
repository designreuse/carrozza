package io.winebox.passaporto.services.routing.ferrovia.traffic;

import io.winebox.passaporto.services.routing.ferrovia.Point;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Created by AJ on 7/25/16.
 */
@AllArgsConstructor
public class RoadEntry {
    @Getter
    private int speed;

    @Getter
    private List<Point> points;
}
