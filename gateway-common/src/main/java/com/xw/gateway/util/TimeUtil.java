package com.xw.gateway.util;

import java.util.concurrent.TimeUnit;

/**
 * 这个是为了避免高并发情况下频繁调用System.currentTimeMillis()做的优化
 */
public class TimeUtil {

    private static volatile long currentTimeMillis;

    static {
        currentTimeMillis = System.currentTimeMillis();
        Thread daemon = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    currentTimeMillis = System.currentTimeMillis();
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (Throwable e) {

                    }
                }
            }
        });
        daemon.setDaemon(true);
        daemon.setName("common-current-time-tick-thread");
        daemon.start();
    }

    public static long currentTimeMillis() {
        return currentTimeMillis;
    }

}
