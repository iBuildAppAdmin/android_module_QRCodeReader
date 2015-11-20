/****************************************************************************
*                                                                           *
*  Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
*                                                                           *
*  This file is part of iBuildApp.                                          *
*                                                                           *
*  This Source Code Form is subject to the terms of the iBuildApp License.  *
*  You can obtain one at http://ibuildapp.com/license/                      *
*                                                                           *
****************************************************************************/
package com.ibuildapp.romanblack.QRPlugin;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Finishes an activity after a period of inactivity if the device is on battery
 * power.
 */
final class InactivityTimer {

    private static final int INACTIVITY_DELAY_SECONDS = 5 * 60;
    private final ScheduledExecutorService inactivityTimer =
            Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
    private final Activity activity;
    private ScheduledFuture<?> inactivityFuture = null;
    private final BroadcastReceiver powerStatusReceiver = new PowerStatusReceiver();

    InactivityTimer(Activity activity) {
        this.activity = activity;
        onActivity();
    }

    void onActivity() {
        cancel();
        if (!inactivityTimer.isShutdown()) {
            try {
                inactivityFuture = inactivityTimer.schedule(new FinishListener(activity),
                        INACTIVITY_DELAY_SECONDS,
                        TimeUnit.SECONDS);
            } catch (RejectedExecutionException ree) {
                // surprising, but could be normal if for some reason the implementation just doesn't
                // think it can shcedule again. Since this time-out is non-essential, just forget it
            }
        }
    }

    public void onPause() {
        cancel();
        try
        {
            activity.unregisterReceiver(powerStatusReceiver);
        } catch (Exception e)
        {
            Log.e("", "");
        }
    }

    public void onResume() {
        activity.registerReceiver(powerStatusReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        onActivity();
    }

    private void cancel() {
        ScheduledFuture<?> future = inactivityFuture;
        if (future != null) {
            future.cancel(true);
            inactivityFuture = null;
        }
    }

    void shutdown() {
        cancel();
        inactivityTimer.shutdown();
    }

    private static final class DaemonThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        }
    }

    private final class PowerStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                // 0 indicates that we're on battery
                // In Android 2.0+, use BatteryManager.EXTRA_PLUGGED
                int batteryPlugged = intent.getIntExtra("plugged", -1);
                if (batteryPlugged > 0) {
                    InactivityTimer.this.cancel();
                }
            }
        }
    }
}