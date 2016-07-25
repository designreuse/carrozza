package io.winebox.passaporto.services.routing.ferrovia.traffic;

import com.graphhopper.GraphHopper;
import io.winebox.passaporto.services.routing.ferrovia.Ferrovia;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by AJ on 7/25/16.
 */
public class RoadDataUpdater {
    private RoadDataSource source;
    private int refreshRate;

    protected RoadDataManager manager;

    public static final class Builder {
        private RoadDataSource source;
        private int refreshRate = 150;

        public Builder setSource( RoadDataSource source ) {
            this.source = source;
            return this;
        }

        public Builder setRefreshRate( int refreshRate ) {
            this.refreshRate = refreshRate;
            return this;
        }

        public RoadDataUpdater build() {
            return new RoadDataUpdater(source, refreshRate);
        }

        public static Builder newInstance( RoadDataSource source ) {
            final Builder builder = new Builder();
            builder.source = source;
            return builder;
        }
    }

    private final AtomicBoolean running = new AtomicBoolean(false);

    protected void start( RoadDataManager manager ) {
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
                        manager.feed(data);
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

    protected void stop() {
        running.set(false);
    }

    private RoadDataUpdater( RoadDataSource source, int refreshRate ) {
        this.source = source;
        this.refreshRate = refreshRate;
    }
}
