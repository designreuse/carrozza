package io.winebox.passaporto.services.routing.ferrovia.traffic;

/**
 * Created by AJ on 7/25/16.
 */
public interface RoadDataSource {
    RoadData fetch() throws Exception;
}
