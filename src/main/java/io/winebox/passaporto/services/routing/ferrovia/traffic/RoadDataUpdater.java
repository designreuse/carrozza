package io.winebox.passaporto.services.routing.ferrovia.traffic;

import io.winebox.passaporto.services.routing.ferrovia.Ferrovia;
import lombok.*;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by AJ on 7/25/16.
 */
@AllArgsConstructor @ToString
public final class RoadDataUpdater {
    private final AtomicBoolean running = new AtomicBoolean(false);
    @Getter private final Ferrovia ferrovia;
    @Getter private final RoadDataSource source;
    @Getter private final int refreshRate;

    public void start() {
        if (running.get()) {
            return;
        }
        running.set(true);
        new Thread("DataUpdater" + refreshRate) {
            @Override
            public void run() {
                System.out.println("fetch new data every " + refreshRate + " seconds");
                while (running.get()) {
                    try {
                        System.out.println("fetch new data");
                        RoadData data = source.fetch();
                        ferrovia.feed(data);
                        try {
                            Thread.sleep(refreshRate * 1000);
                        } catch (InterruptedException ex) {
                            System.out.println("update thread stopped");
                            break;
                        }
                    } catch (Exception ex) {
                        System.out.println(ex);
                        System.out.println("Problem while fetching data");
                    }
                }
            }
        }.start();
    }

    public void stop() {
        running.set(false);
    }
}