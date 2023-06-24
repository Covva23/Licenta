package com.example.licenta;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class TimerService extends Service {
    private static final String CHANNEL_ID = "Channel 1";
    private static final int NOTIFICATION_ID = 1;

    private CountDownTimer timer;
    private long timerDuration;

    public TimerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        timerDuration = 6 * 60 * 60 * 1000;

        startTimer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground(NOTIFICATION_ID, createNotification());

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (timer != null) {
            timer.cancel();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    private void startTimer() {
        timer = new CountDownTimer(timerDuration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Intent updateIntent = new Intent("TIMER_UPDATE");
                updateIntent.putExtra("timeRemaining", millisUntilFinished);
                LocalBroadcastManager.getInstance(TimerService.this).sendBroadcast(updateIntent);
            }

            @Override
            public void onFinish() {
                stopSelf();
            }
        }.start();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Canal pentru timer";
            String description = "Canal pentru serviciul de timp";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent intent = new Intent(this, Activitatea.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Licenta")
                .setContentText("Timpul a expirat, utilizati telefonul pentru a confirma ca totul este in regula.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOngoing(true);

        return builder.build();
    }
}
