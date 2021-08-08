package com.cyh.timetable;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class WebExRedirectWorker extends Worker {
    public WebExRedirectWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public final String NOTIFICATION_CHANNEL = "TimeTable_01";

    @NonNull
    @Override
    public Result doWork() {
        sendNotification();
        return Result.success();
    }

    public void sendNotification(){

        boolean isVibe = getInputData().getBoolean("mode",true);
        String subjectTitle = getInputData().getString("Title");
        int timeBefore = getInputData().getInt("Time",0);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(getInputData().getString("WebExURI")));

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationCompat = new NotificationCompat
                .Builder(getApplicationContext(), NOTIFICATION_CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle(subjectTitle)
                .setContentText(subjectTitle + "수업이 "+ timeBefore + "분 뒤에 시작합니다.")
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(PendingIntent.getActivities(getApplicationContext(),0, new Intent[]{intent},0))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL,"TimeTable", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            if(isVibe){
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100,100,100,100});
            }
            else{
                Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                channel.setSound(ringtone,audioAttributes);
            }
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationCompat.build());


    }
}
