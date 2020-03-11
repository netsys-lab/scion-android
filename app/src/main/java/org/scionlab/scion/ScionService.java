/*
 * Copyright (C) 2019-2020 Vera Clemens, Tom Kranz, Tom Heimbrodt, Elias Kuiter
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.scionlab.scion;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.NotificationCompat;

import org.scionlab.scion.as.ScionAS;
import org.scionlab.scion.as.ScionLabAS;

import java.util.stream.Collectors;

import timber.log.Timber;

public class ScionService extends Service {
    private static final int NOTIFICATION_ID  = 1;
    private static final String NOTIFICATION_CHANNEL = ScionService.class.getCanonicalName() + ".NOTIFICATION_CHANNEL";
    private static final String SCIONLAB_CONFIGURATION = ScionService.class.getCanonicalName() + ".SCIONLAB_CONFIGURATION";
    private static final String PING_ADDRESS = ScionService.class.getCanonicalName() + ".PING_ADDRESS";
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private Handler handler;
    private ScionLabAS scionLabAS;
    private static ScionAS.State state;

    static void start(Context context, String scionLabConfiguration, String pingAddress) {
        context.startService(new Intent(context, ScionService.class)
                .putExtra(SCIONLAB_CONFIGURATION, scionLabConfiguration)
                .putExtra(PING_ADDRESS, pingAddress));
    }

    static void stop(Context context) {
        context.stopService(new Intent(context, ScionService.class));
    }

    static ScionAS.State getState() {
        return state;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setupNotification();
        HandlerThread handlerThread = new HandlerThread("ScionService");
        handlerThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler(this));
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        handler = new Handler(looper);
        scionLabAS = new ScionLabAS(this, (state, componentState) -> {
            ScionService.state = state;
            MainActivity.updateUserInterface(this, state);
            notify(state, "SCION: " + state + "\n" +
                    componentState.entrySet().stream()
                            .map(e -> e.getKey() + ": " + e.getValue())
                            .collect(Collectors.joining("\n")));
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int ret = super.onStartCommand(intent, flags, startId);
        if (intent == null || scionLabAS.getState() != ScionAS.State.STOPPED)
            return ret;

        final String scionLabConfiguration = intent.getStringExtra(SCIONLAB_CONFIGURATION);
        if (scionLabConfiguration == null) {
            Timber.e("no SCIONLab configuration given");
            return ret;
        }

        final String pingAddress = intent.getStringExtra(PING_ADDRESS);
        if (pingAddress == null) {
            Timber.e("no ping address given");
            return ret;
        }

        handler.post(() -> {
            // make this a foreground service, decreasing the probability that Android arbitrarily kills this service
            startForeground(NOTIFICATION_ID, notificationBuilder.build());
            scionLabAS.start(scionLabConfiguration, pingAddress);
        });

        return ret;
    }

    @Override
    public void onDestroy() {
        if (scionLabAS.getState() == ScionAS.State.STOPPED)
            return;

        handler.post(() -> {
            scionLabAS.stop();
            stopForeground(STOP_FOREGROUND_REMOVE);
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setupNotification() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL, getString(R.string.applicationName), NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.notificationChannelDescription));
            notificationManager.createNotificationChannel(channel);
        }
        notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_scion_logo)
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        MainActivity.bringToForeground(this), 0));
    }

    private void notify(ScionAS.State state, String text) {
        if (state != ScionAS.State.STOPPED) {
            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        }
    }
}
