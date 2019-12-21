package leaf.prod.app.receiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import androidx.core.app.NotificationCompat;

import leaf.prod.app.R;
import leaf.prod.app.activity.wallet.MainActivity;
import leaf.prod.walletsdk.model.response.relay.Transaction;

public class NotificationReceiver extends BroadcastReceiver {

    private static final int NOTIFY_ID = 1;

    private static final String CHANNEL_ID = "my_channel_01";

    private static NotificationReceiver notificationReceiver;

    private Context context;

    private NotificationReceiver(Context context) {
        this.context = context;
    }

    public static NotificationReceiver getInstance(Context context) {
        if (notificationReceiver == null) {
            return new NotificationReceiver(context);
        }
        return notificationReceiver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Transaction tx = (Transaction) intent.getSerializableExtra("pending_tx");
        String title = context.getString(R.string.transaction_success);
        String text = tx.getTxHash();
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_share_logo);
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClass(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) SystemClock.uptimeMillis(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        String image = String.format("icon_tx_%s", tx.getType().getDescription().toLowerCase());
        int identifier = context.getResources().getIdentifier(image, "mipmap", context.getPackageName());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setLargeIcon(largeIcon)
                .setSmallIcon(identifier)
                .setTicker(context.getString(R.string.app_name))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                CharSequence name = context.getString(R.string.eth_notify);
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                notificationManager.createNotificationChannel(mChannel);
            }
            notificationManager.notify(NOTIFY_ID, builder.build());
        }
    }
}
