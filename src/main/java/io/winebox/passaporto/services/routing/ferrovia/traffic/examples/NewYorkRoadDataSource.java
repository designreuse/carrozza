package io.winebox.passaporto.services.routing.ferrovia.traffic.examples;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.winebox.passaporto.services.routing.ferrovia.Point;
import io.winebox.passaporto.services.routing.ferrovia.traffic.RoadData;
import io.winebox.passaporto.services.routing.ferrovia.traffic.RoadDataSource;
import io.winebox.passaporto.services.routing.ferrovia.traffic.RoadEntry;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AJ on 7/25/16.
 */
public class NewYorkRoadDataSource implements RoadDataSource {

    public RoadData fetch() throws Exception {
        JSONArray arr = new JSONArray(fetchJSONString("http://localhost:3000/traffic"));
        RoadData data = new RoadData();

        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            double speed = obj.getDouble("speed");
            String idStr = obj.getString("road_entry_id");

            JSONArray paths = obj.getJSONArray("points");
            List<Point> points = new ArrayList<Point>();
            for (int pointIndex = 0; pointIndex < paths.length(); pointIndex++) {
                JSONObject point = paths.getJSONObject(pointIndex);
                points.add(new Point(point.getDouble("latitude"), point.getDouble("longitude")));
            }

            if (!points.isEmpty()) {
                data.add(new RoadEntry((int)speed, points));
            }
        }
        return data;
    }

    private String fetchJSONString(String url) throws UnirestException {
        String string = Unirest.post(url)
                .header("accept", "application/json").asString().getBody();
        return string;
    }
}
