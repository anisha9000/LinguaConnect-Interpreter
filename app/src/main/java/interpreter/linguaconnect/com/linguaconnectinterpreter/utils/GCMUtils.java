package interpreter.linguaconnect.com.linguaconnectinterpreter.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import interpreter.linguaconnect.com.linguaconnectinterpreter.BuildConfig;
import interpreter.linguaconnect.com.linguaconnectinterpreter.R;
import interpreter.linguaconnect.com.linguaconnectinterpreter.services.RegistrationIntentService;
import interpreter.linguaconnect.com.linguaconnectinterpreter.ui.LinguaConnect;

/**
 * Created by anisha on 13/1/16.
 */
public class GCMUtils {

    private static final String TAG = GCMUtils.class.getName();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static void issueGCMRegistration(final Context context) {

        if (MpGooglePlayServices.checkPlayServices(context)) {
            final String regId = getRegistrationId(context);
            Log.e("GCMUtils:", "regId = "+regId);
            if (regId == null || regId.equals("")) {
                Log.e("GCMUtils", "regId = null or something");
                registerInBackground(context, "");
                /*NotificationMetadataModel.getInstance().getFromServer(context, new ModelCallback<JSONObject>() {

                    @Override
                    public void success(JSONObject response) {

                        String sender = response.optString("google_project_id", null);
                        Log.e("GCMUtils: ", "sender = " + sender);
                        Log.e(TAG,"getfromserver:"+response);
                        if (sender != null) {
                            registerInBackground(context, sender);
                        } else {
                            Log.e(TAG, "GCM project id not found.");
                        }
                    }

                    @Override
                    public void error(Throwable throwable) {
                        Log.e(TAG, "Failed to register in GCM.", throwable);
                    }
                });*/
            }
            else {
                Log.e(TAG,"sendtopusher");
                Log.e(TAG, "sendtopusher");
                RegistrationIntentService.sendToPusher(context, regId);
            }
        }
        else {
            Log.e(TAG, "No valid Google Play Services APK found.");
        }
    }

    /**
     * This method register for GCM services in the background mode.
     *
     * @param context
     * @param senderId
     * */
    public static void registerInBackground(final Context context, final String senderId) {

        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... params) {

                String senderId = context.getString(R.string.gcm_defaultSenderId);	// project sender id
                String regId = null;

                try {
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
                    regId = gcm.register(senderId);	// new GCM registration id
                    Log.e(TAG,"received gcm key:"+regId);
                    // register on server and save on disk.
                    RegistrationIntentService.registerOnPusher(regId);
                } catch (IOException e) {
                    Log.e(TAG, "GCM Registration failed.", e);
                }

                return regId;
            }
        }.execute(senderId);
    }

    /**
     * This method unregister from the GCM services and also pusher
     * server in the background mode.
     *
     * @param context
     * */
    public static void unregisterInBackground(final Context context) {

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {

                try {
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
                    gcm.unregister();

                    // Unregister the GCM info from pusher server.
                    String regId = Utility.getLocalString(context, Utility.CURRENT_GCM_ID);
                    RegistrationIntentService.removeFromPusher(context, regId);
                } catch (IOException e) {
                    Log.e(TAG, "GCM Un-registration failed.", e);
                }

                return null;
            }
        }.execute();
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     *         */
    private static String getRegistrationId(final Context context) {

        String registrationId = Utility.getLocalString(context, Utility.CURRENT_GCM_ID);
        if (registrationId == null || registrationId.equals("")) {
            Log.e(TAG, "Registration not found.");
            return "";
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = Integer.MIN_VALUE;
        int currentVersion;

        currentVersion = BuildConfig.VERSION_CODE;
        if (Utility.getLocalString(context, Utility.APP_VERSION) != null) {
            registeredVersion = Integer.valueOf(Utility.getLocalString(context, Utility.APP_VERSION));
        }

        if (registeredVersion != currentVersion) {
            Log.e(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    public static NotificationCompat.Builder createNotificationBuilder(Context context) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setLights(context.getResources().getColor(R.color.primary), 1000, 1000);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        boolean doVibrate = sharedPrefs.getBoolean("pref_key_enable_vibration", true);
        if (doVibrate) {
            builder.setVibrate(new long[] {500, 500, 500});
        }

        boolean playAudio = sharedPrefs.getBoolean("pref_key_enable_sound", true);
        if (playAudio) {
            /*Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.cp);
            builder.setSound(soundUri);*/
        }

        setPriority(builder);
        //builder.setDeleteIntent(getDeleteIntent(context));

        return builder;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static void setPriority(NotificationCompat.Builder mBuilder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
        }
    }

    public static Notification build(Context context,
                                     NotificationCompat.Builder mBuilder, Intent resultIntent) {
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(LinguaConnect.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        return notification;
    }
}
