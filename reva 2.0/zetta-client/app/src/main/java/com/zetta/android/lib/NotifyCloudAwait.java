package com.zetta.android.lib;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import java.util.Date;
import java.util.List;

/**
 * Created by ME on 2017/09/25.
 */

public class NotifyCloudAwait {
    public void end(DISMISS_TYPE type) {
    }
    public boolean isCtxActive(){
        ActivityManager activityManager = (ActivityManager)ct.getSystemService(ct.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = activityManager.getRunningTasks(Integer.MAX_VALUE);
        for(ActivityManager.RunningTaskInfo task : taskList){
            if(ct.getPackageName().equals(task.baseActivity.getPackageName())){
                return true;
            }
        }
        return false;
    }

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
            public void work(Context ct) {
                startTime =  new Date().getTime();
                if (!dismissed) {
                    String waitMsg = message;
                    connectToCloudWaitDialog = new ProgressDialog(ct) {
                        @Override
                        public void onBackPressed() {
                            if (cancelable) {
                                doEnd(DISMISS_TYPE.USER, true);
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
                            NotifyCloudAwait.this.doEnd(NotifyCloudAwait.DISMISS_TYPE.TIMEOUT, false);
                        }
                    }
                };
            }
        };
    }

    private void doEnd(final DISMISS_TYPE type, boolean force) {
        if(force){
            connectToCloudWaitDialog.dismiss();
        }
        dismissed = true;
        long delta = new Date().getTime() - startTime;
        if(delta < MIN_DIALOG_SHOW_TIME){
            new DelayedUiAction(ct, (int)delta) {
                @Override
                public void work(Context ct) {
                    if (connectToCloudWaitDialog != null) {
                        Log.d("---BUG-TRACK---NCA", "LONG");
                        if(isCtxActive()) {
                            Log.d("---BUG-TRACK---NCA", "ACTIVE");
                            connectToCloudWaitDialog.dismiss();
                        }else{
                            Log.d("---BUG-TRACK---NCA", "INACTIVE");
                        }
                    }
                }
            };
        }else if (connectToCloudWaitDialog != null) {
            Log.d("---BUG-TRACK---NCA", "SHORT");
            connectToCloudWaitDialog.dismiss();
        }
        ((Activity) ct).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                end(type);
            }
        });
    }


    final public void dismiss(boolean force) {
        doEnd(DISMISS_TYPE.PROGRAM, force);
    }

    private static final int MIN_DIALOG_SHOW_TIME = 1000;
    private boolean dismissed = false;
    ProgressDialog connectToCloudWaitDialog;
}
