package interpreter.linguaconnect.com.linguaconnectinterpreter.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONException;
import org.json.JSONObject;

import interpreter.linguaconnect.com.linguaconnectinterpreter.AppController;
import interpreter.linguaconnect.com.linguaconnectinterpreter.BuildConfig;
import interpreter.linguaconnect.com.linguaconnectinterpreter.R;
import interpreter.linguaconnect.com.linguaconnectinterpreter.utils.Constants;
import interpreter.linguaconnect.com.linguaconnectinterpreter.utils.Utility;

/**
 * Created by anisha on 10/1/16.
 */
public class RegistrationIntentService extends IntentService {

    public void onCreate() {
        super.onCreate();
        Log.d("Server", ">>>onCreate()");
    }

    public static final String GCM_NOTIFICATION_EVENT = "GCM_NOTIFICATION_EVENT";
    private static final String TAG = RegistrationIntentService.class.getName();

    public RegistrationIntentService() {
        super("RegistrationIntentService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, startId, startId);
        Log.e("LocalService", "Received start id " + startId + ": " + intent);

        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent remoteIntent) {

        if(remoteIntent!=null) {
            Log.e(TAG, "GCM reveived " + remoteIntent.toString());
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            String messageType = gcm.getMessageType(remoteIntent);
            Bundle extras = remoteIntent.getExtras();
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Log.e(TAG, "creating clevertap notification");
            }

            try {
                synchronized (TAG) {
                    // Initially a network call, to retrieve the token, subsequent calls are local.
                    InstanceID instanceID = InstanceID.getInstance(this);
                    String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    Log.e(TAG, "GCM Registration Token: " + token);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to complete token refresh", e);
            }

            if (!extras.isEmpty()) {
                if (GoogleCloudMessaging.
                        MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                    Log.e(TAG, "Send error: " + extras.toString());
                } else if (GoogleCloudMessaging.
                        MESSAGE_TYPE_DELETED.equals(messageType)) {
                    Log.e(TAG, "Deleted messages on server: " + extras.toString());
                } else if (GoogleCloudMessaging.
                        MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                    Log.e(TAG, "regular GCM message");
                    // If it's a regular GCM message, do some work.

                }
            }
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        //GCMBroadcastReceiver.completeWakefulIntent(remoteIntent);
    }

    public static void registerOnPusher(String regId) {
        Log.e(TAG, "registerOnPusher called");

        final Context context = AppController.getInstance();
        try {
            sendToPusher(context, regId);

            String currentRegId = Utility.getLocalString(context, Utility.CURRENT_GCM_ID);
            if (currentRegId != null && !regId.equals(currentRegId)) {
                removeFromPusher(context, currentRegId);
            }

            int appVersion = BuildConfig.VERSION_CODE;
            Utility.saveLocalString(context, Utility.CURRENT_GCM_ID, regId);
            Utility.saveLocalString(context, Utility.APP_VERSION, String.valueOf(appVersion));
        } catch (Exception e) {
            Log.e(TAG, "App Version code not found."+ e);
        }
    }

    public static void sendToPusher(final Context context, String regId) {
        Log.e(TAG, "sendToPusher");
        JSONObject obj = new JSONObject();
        try {
            obj.put(context.getString(R.string.type), "interpreter");
            obj.put(context.getString(R.string.email), Utility.getLocalString(context, Constants.USER_EMAIL));
            obj.put(context.getString(R.string.reg_id), regId);
            Log.e(TAG, "json obj = "+ obj);
        } catch (JSONException e) {
            Log.e(TAG, "Failure to create json obj", e);
            return;
        }

        //Send data to pusher
        String url = Constants.BASE_URL + Constants.REGISTER_GCM;

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, obj,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG,"response:"+response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                VolleyError error = null;
                if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                    error = new VolleyError(new String(volleyError.networkResponse.data));
                    //Log.e(TAG,"error:"+error+", volleyError:"+volleyError);
                }
                try {
                    JSONObject responseString = new JSONObject(error.getMessage());
                    Log.e(TAG,"responseStr:"+responseString);
                } catch (JSONException e) {
                    Log.e(TAG,"Exception:"+e);
                    e.printStackTrace();
                }
            }
        }) {

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {

                return volleyError;
            }
        };

        DefaultRetryPolicy policy = new DefaultRetryPolicy(2 * 60 * 1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjReq.setRetryPolicy(policy);

        AppController.getInstance().addToRequestQueue(jsonObjReq);

    }

    public static void removeFromPusher(final Context context, String regId) {

        //Remove from pusher
/*
        Log.e(TAG, "removeFromPusher");
        JSONObject obj = new JSONObject();
        try {
            obj.put(context.getString(R.string.type), "interpreter");
            obj.put(context.getString(R.string.email), Utility.getLocalString(context, Constants.USER_EMAIL));
            obj.put(context.getString(R.string.reg_id), regId);
            Log.e(TAG, "json obj = "+ obj);
        } catch (JSONException e) {
            Log.e(TAG, "Failure to create json obj", e);
            return;
        }

        //Send data to pusher
        String url = Constants.BASE_URL + Constants.UNREGISTER_GCM;

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, obj,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG,"response:"+response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                VolleyError error = null;
                if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                    error = new VolleyError(new String(volleyError.networkResponse.data));
                    //Log.e(TAG,"error:"+error+", volleyError:"+volleyError);
                }
                try {
                    JSONObject responseString = new JSONObject(error.getMessage());
                    Log.e(TAG,"responseStr:"+responseString);
                } catch (JSONException e) {
                    Log.e(TAG,"Exception:"+e);
                    e.printStackTrace();
                }
            }
        }) {

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {

                return volleyError;
            }
        };

        DefaultRetryPolicy policy = new DefaultRetryPolicy(2 * 60 * 1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjReq.setRetryPolicy(policy);

        AppController.getInstance().addToRequestQueue(jsonObjReq);*/
    }
}
