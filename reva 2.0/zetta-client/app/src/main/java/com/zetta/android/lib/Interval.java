package com.zetta.android.lib;

/**
 * Created by ME on 2017/09/25.
 */

public class Interval {
    public void begin(){}
    public void end() { }
    public void work(){ }

    public void interrupt(){
        worker.interrupt();
    }
    public void clearInterval() {
        callEnd();
        sleepDuration = 0;
    }

    public Interval(final int sleep_, final int max) {
        sleepDuration = sleep_;
        steps = max;
        worker = new Thread() {
            @Override
            public void run() {
                while (sleepDuration > 0 && steps-- > 0) {
                    work();
                    try {
                        Thread.sleep(sleepDuration);
                    } catch (InterruptedException e) {
                    }
                }
                callEnd();
            }
        };
        worker.start();
        begin();
    }

    int steps = 0;
    int sleepDuration;

    private void callEnd(){
        if(!ended){
            ended = true;
            end();
        }
    }

    boolean ended = false;
    private Thread worker = null;
}
