package com.zetta.android.lib;

import android.app.Activity;
import android.content.Context;

/**
 * Created by ME on 2017/09/25.
 */

public abstract class DelayedUiAction {
    public DelayedUiAction(final Context ct, final int sleep) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                } finally {
                    ((Activity) ct).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            work();
                        }
                    });
                }
            }
        }.start();
    }

    public abstract void work();
}