package nogor.pay;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import java.util.ArrayList;
import java.util.HashMap;

public class MyBackgroundService extends Service {
    private static final String CHANNEL_ID = "SMS_MONITOR_CHANNEL";
    private static final int NOTIFICATION_ID = 1001;

    private DatabaseHelper databaseHelper;
    private NetworkChangeReceiver networkReceiver;

    private final BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isNetworkConnected()) {
                updateNotification("Online - Monitoring SMS", "SMS Monitor Active");
                syncPendingData();
            } else {
                updateNotification("Offline - No Internet", "SMS Monitor Active");
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        databaseHelper = new DatabaseHelper(this);
        createNotificationChannel();
        registerNetworkReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());
        return START_STICKY; // Restart if killed
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterNetworkReceiver();
        // Restart the service
        Intent restartIntent = new Intent(this, MyBackgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartIntent);
        } else {
            startService(restartIntent);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "SMS Monitor Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Monitors SMS from allowed contacts");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        String status = isNetworkConnected() ? "Online - Monitoring SMS" : "Offline - No Internet";

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SMS Monitor Active")
                .setContentText(status)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private void updateNotification(String contentText, String title) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .build();

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, notification);
    }

    private void registerNetworkReceiver() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityReceiver, filter);
    }

    private void unregisterNetworkReceiver() {
        try {
            unregisterReceiver(connectivityReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver not registered
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void syncPendingData() {
        // Sync any pending SMS data to server
        ArrayList<HashMap<String, String>> pendingSms = databaseHelper.getAllSms();

        for (HashMap<String, String> sms : pendingSms) {
            String sender = sms.get("sender");
            String message = sms.get("message");
            String id = sms.get("id");

            ApiRequest.sendSmsData(this, message, sender, new ApiRequest.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    // Delete from local database after successful sync
                    databaseHelper.deleteSms(Long.parseLong(id));
                }

                @Override
                public void onError(String error) {
                    // Keep in database for retry later
                }
            });
        }
    }
}