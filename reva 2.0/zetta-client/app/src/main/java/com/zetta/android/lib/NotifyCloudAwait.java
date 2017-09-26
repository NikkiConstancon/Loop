package com.zetta.android.lib;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import java.util.Date;

/**
 * Created by ME on 2017/09/25.
 */

public class NotifyCloudAwait {
    NotifyCloudAwait self = this;

    public enum DISMISS_TYPE {TIMEOUT, USER, PROGRAM}

    Context ct;
    long startTime = 0;
    public NotifyCloudAwait(final Context ct_, final boolean cancelable,
                            int delay, final String message,
                            final int maxWaitTme
    ) {
        ct = ct_;
        new DelayedUiAction(ct, delay) {
            @Override
            public void work() {
                startTime =  new Date().getTime();
                if (!dismissed) {
                    String waitMsg = message;
                    connectToCloudWaitDialog = new ProgressDialog(ct) {
                        @Override
                        public void onBackPressed() {
                            if (cancelable) {
                                doEnd(DISMISS_TYPE.USER);
                                super.onBackPressed();
                            }
                        }
                    };
                    connectToCloudWaitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    connectToCloudWaitDialog.setMessage(waitMsg);
                    connectToCloudWaitDialog.setIndeterminate(true);
                    connectToCloudWaitDialog.setCanceledOnTouchOutside(false);

                    connectToCloudWaitDialog.show();
                }
                new Interval(maxWaitTme, 1) {
                    @Override
                    public void end() {
                        if(!NotifyCloudAwait.this.dismissed) {
                            NotifyCloudAwait.this.doEnd(NotifyCloudAwait.DISMISS_TYPE.TIMEOUT);
                        }
                    }
                };
            }
        };
    }

    private void doEnd(final DISMISS_TYPE type) {
        dismissed = true;
        long delta = new Date().getTime() - startTime;
        if(delta < MIN_DIALOG_SHOW_TIME){
            new DelayedUiAction(ct, (int)delta) {
                @Override
                public void work() {
                    if (connectToCloudWaitDialog != null) {
                        connectToCloudWaitDialog.dismiss();
                    }
                }
            };
        }else if (connectToCloudWaitDialog != null) {
            connectToCloudWaitDialog.dismiss();
        }
        ((Activity) ct).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                end(type);
            }
        });
    }

    public void end(DISMISS_TYPE type) {
    }

    final public void dismiss() {
        doEnd(DISMISS_TYPE.PROGRAM);
    }

    private static final int MIN_DIALOG_SHOW_TIME = 1000;
    private boolean dismissed = false;
    ProgressDialog connectToCloudWaitDialog;
}
