package interpreter.linguaconnect.com.linguaconnectinterpreter.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.Set;

import interpreter.linguaconnect.com.linguaconnectinterpreter.R;
import interpreter.linguaconnect.com.linguaconnectinterpreter.ui.LinguaConnect;
import interpreter.linguaconnect.com.linguaconnectinterpreter.ui.LoginActivity;

/**
 * Created by anisha on 10/1/16.
 */
public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    public final static String BROADCAST_ACTION = "receivedNotification";

    private void processContent(JSONObject data, String notif_type, Intent intent, Bitmap bm) {
        Log.e(TAG, "inside processContent: data = " + data + ", notif_type = " + notif_type + ", intent = " + intent);
        if (notif_type.equalsIgnoreCase("text")) {
            try {
                sendNotification(data.optString("message"), 1, intent, bm, null);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else if (notif_type.equalsIgnoreCase("image")) {
            Log.e(TAG, "notif type is image");
            try {
                JSONObject content = new JSONObject(data.getString("content"));
                if (content.has("media_url")) {
                    if (!content.getString("media_url").equalsIgnoreCase("")) {
                        sendNotification(content.getString("media_url"), 2, intent, bm, null);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else if (notif_type.equalsIgnoreCase("text_image")) {
            try {
                JSONObject content = new JSONObject(data.getString("content"));
                String txt = "", media_url = "";
                if (content.has("text")) {
                    txt = content.getString("text");
                }

                if (content.has("media_url")) {
                    media_url = content.getString("media_url");
                }
                if (txt.equalsIgnoreCase("") && media_url.equalsIgnoreCase("")) {

                } else if (media_url.equalsIgnoreCase("")) {
                    sendNotification(txt, 1, intent, bm, null);
                } else if (txt.equalsIgnoreCase("")) {
                    sendNotification(media_url, 2, intent, bm, null);
                } else {
                    sendNotification(txt, media_url, intent, bm,null);
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }

    }

    @Override
    public void onMessageReceived(String from, Bundle dataBundle) {
        Log.e(TAG, "onMessageReceived called");
        Log.e(TAG, "from = " + from);
        Log.e(TAG, "data bundle= " + dataBundle);
        Calendar c = Calendar.getInstance();
        int mins = c.get(Calendar.MINUTE);
        int hrs = c.get(Calendar.HOUR_OF_DAY);
        int dt = c.get(Calendar.DATE);
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        String month_name = month_date.format(c.getTime());

        if (dataBundle == null) return;
        try {
            JSONObject data = new JSONObject();
            Set<String> keys = dataBundle.keySet();
            for (String key : keys) {
                Log.e(TAG,"data bundle key:"+key);
                // json.put(key, bundle.get(key)); see edit below
                data.put(key, dataBundle.get(key).toString());

            }
            Log.e(TAG, "JSONOBJ HAS BEEN CREATED");
            Log.e(TAG, "data = " + data);
            //JSONObject data = new JSONObject(dataBundle.toString());
            Bitmap defaultIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_notification_icon);

            String event = data.getString("event");
            if (event.equals(null)) {
                event = data.getString("\"event\"");
            }
            Log.e(TAG, "event = " + event);

            Intent BroadcastIntent = new Intent(BROADCAST_ACTION);
            BroadcastIntent.putExtra("event", event);
            BroadcastIntent.putExtra("booking_id", data.optString("booking_id"));
            Log.e(TAG,"send broadcast");
            this.sendBroadcast(BroadcastIntent);

            Class page = LinguaConnect.class;
            Intent intent = new Intent(this, page);
            intent.putExtra("event", event);
            intent.putExtra("booking_id",data.optString("booking_id"));

                processContent(data, "text", intent, defaultIcon);

        } catch ( Exception e )
        {
            Log.e(TAG, "ERROR: " + e);
        }
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message, int flag, Intent intent, Bitmap bm, String title) {
        Log.e(TAG, "inside sendNotification");
        Log.e(TAG, "flag = " + flag);
        Log.e(TAG, "intent = " + intent);
        if (flag == 1) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setLargeIcon(bm)
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setContentTitle((title!=null)?title:getString(R.string.app_name))
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message));
            final Notification notification = notificationBuilder.build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Random random = new Random();
            int m = random.nextInt(9999 - 1000) + 1000;
            notificationManager.notify(m, notification);

        } else if (flag == 2) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            try {
                Bitmap remote_picture = null;
                NotificationCompat.BigPictureStyle notiStyle = new NotificationCompat.BigPictureStyle();
                try {
                    remote_picture = BitmapFactory.decodeStream((InputStream) new URL(message).getContent());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                notiStyle.bigPicture(remote_picture);
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                        .setLargeIcon(bm)
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setContentTitle((title!=null)?title:getString(R.string.app_name))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setStyle(notiStyle);
                final Notification notification = notificationBuilder.build();
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                Random random = new Random();
                int m = random.nextInt(9999 - 1000) + 1000;
                notificationManager.notify(m, notification);
            } catch (Exception e) {
            }
        }
    }

    private void sendNotification(String message, String img_url, Intent intent, Bitmap bm, String title) {
        Log.e(TAG, "message = " + message);
        Log.e(TAG, "img url = " + img_url);
        Log.e(TAG, "intent = " + intent);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        try {
            Bitmap remote_picture = null;
            NotificationCompat.BigPictureStyle notiStyle = new NotificationCompat.BigPictureStyle();
            try {
                remote_picture = BitmapFactory.decodeStream((InputStream) new URL(img_url).getContent());
            } catch (Exception e) {
                Log.e(TAG,"exception in fetching picture:"+e);
                e.printStackTrace();
            }
            notiStyle.bigPicture(remote_picture);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setLargeIcon(bm)
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setContentTitle((title!=null)?title:getString(R.string.app_name))
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .setStyle(notiStyle);
            final Notification notification = notificationBuilder.build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Random random = new Random();
            int m = random.nextInt(9999 - 1000) + 1000;
            notificationManager.notify(m, notification);
        } catch (Exception e) {
        }
    }
}
