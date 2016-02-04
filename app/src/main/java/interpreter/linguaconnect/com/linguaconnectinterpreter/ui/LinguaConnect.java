package interpreter.linguaconnect.com.linguaconnectinterpreter.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import interpreter.linguaconnect.com.linguaconnectinterpreter.AppController;
import interpreter.linguaconnect.com.linguaconnectinterpreter.BuildConfig;
import interpreter.linguaconnect.com.linguaconnectinterpreter.R;
import interpreter.linguaconnect.com.linguaconnectinterpreter.fragments.fragmentDrawer;
import interpreter.linguaconnect.com.linguaconnectinterpreter.services.MyGcmListenerService;
import interpreter.linguaconnect.com.linguaconnectinterpreter.utils.Constants;
import interpreter.linguaconnect.com.linguaconnectinterpreter.utils.HandleProgressBar;
import interpreter.linguaconnect.com.linguaconnectinterpreter.utils.Utility;

public class LinguaConnect extends AppCompatActivity implements fragmentDrawer.FragmentDrawerListener {

    private String TAG = "LinguaConnect";
    private fragmentDrawer drawerFragment;
    public boolean isDrawerOpened;
    private String event;
    private String bookingId;
    private TextView tvCurrentBooking;
    private Button btChangeBooking;
    static boolean isBookingReceived = false;

    ImageView ivEventImage;
    TextView tvEventName;
    TextView tvEventLocation;
    TextView tvEventTiming;
    TextView tvEventDescription;
    private String eventId;

    Chronometer counter;
    int currentRating = 0;
    ImageView[] star;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lingua_connect);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerFragment = new fragmentDrawer();
        drawerFragment = (fragmentDrawer) getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.feed_activity), toolbar);
        drawerFragment.setDrawerListener(this);

        if (drawerFragment != null) {
            drawerFragment.setProfilePic();
        }

        Log.e(TAG,"onCreate : register listener");
        registerReceiver(broadcastReceiver, new IntentFilter(MyGcmListenerService.BROADCAST_ACTION));

        if(isLoginMade()) {
            Log.e(TAG,"lingua connect page");
            if(bookingId != null && !bookingId.isEmpty()) {
                getCurrentBooking();
            }
            initScreen();
            fetchEvent();
        }

        Intent intent = getIntent();
        if (intent != null) {
            event = intent.getStringExtra("event");
            if(intent.hasExtra("booking_id")) {
                bookingId = intent.getStringExtra("booking_id");
            }
            if(event != null && !event.isEmpty()) {
                Utility.saveLocalString(LinguaConnect.this, Constants.currentEvent, event);
                updateUi();
            }
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            event = intent.getStringExtra("event");
            if(intent.hasExtra("booking_id")) {
                bookingId = intent.getStringExtra("booking_id");
            }

            Utility.saveLocalString(LinguaConnect.this, Constants.currentEvent, event);

            updateUi();
            Log.e(TAG,"broadcast recieved:"+event);
        }
    };

    private void updateUi() {
        if(Utility.getLocalString(this, Constants.currentEvent).isEmpty()) {
            findViewById(R.id.current_booking).setVisibility(View.GONE);
            findViewById(R.id.eventLayout).setVisibility(View.VISIBLE);
            isBookingReceived = false;
            Log.e(TAG,"isBooking recieved on updateUi:"+isBookingReceived);
        } else if (Utility.getLocalString(this,Constants.currentEvent).contains("create_booking")) {
            isBookingReceived = true;
            Log.e(TAG,"isBooking recieved on updateUi:"+isBookingReceived);
            findViewById(R.id.current_booking).setVisibility(View.VISIBLE);
            findViewById(R.id.eventLayout).setVisibility(View.GONE);
            findViewById(R.id.change_booking).setVisibility(View.VISIBLE);
            findViewById(R.id.chronometer).setVisibility(View.GONE);
            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            p.addRule(RelativeLayout.BELOW, R.id.current_booking_text);
            btChangeBooking.setLayoutParams(p);
        } else if (Utility.getLocalString(this,Constants.currentEvent).equals("cancel_booking") ||
                Utility.getLocalString(this,Constants.currentEvent).equals("end_booking")) {
            isBookingReceived = false;
            findViewById(R.id.current_booking).setVisibility(View.GONE);
            findViewById(R.id.eventLayout).setVisibility(View.VISIBLE);
            if(counter != null) {
                counter.stop();
            }
            showRatingDialog();
            Utility.saveLocalString(this, Constants.currentEvent, "");
        } else if(Utility.getLocalString(this,Constants.currentEvent).equals("start_booking")) {
            isBookingReceived = true;
            Log.e(TAG,"isBooking recieved on updateUi:"+isBookingReceived);
            findViewById(R.id.current_booking).setVisibility(View.VISIBLE);
            findViewById(R.id.eventLayout).setVisibility(View.GONE);
            findViewById(R.id.chronometer).setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            p.addRule(RelativeLayout.BELOW, R.id.chronometer);
            btChangeBooking.setLayoutParams(p);

            Log.e(TAG,"booking started. remove cancel button and add timer.");
        }
    }

    private void showRatingDialog() {
        star = new ImageView[5];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.rate_alert_box_layout, null);
        star[0] = (ImageView) view.findViewById(R.id.star0);
        star[1] = (ImageView) view.findViewById(R.id.star1);
        star[2] = (ImageView) view.findViewById(R.id.star2);
        star[3] = (ImageView) view.findViewById(R.id.star3);
        star[4] = (ImageView) view.findViewById(R.id.star4);

        for (int i = 0; i <= 4; i++) {
            star[i].setSelected(false);
        }

        star[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Alert", "star0 clicked");

                currentRating = 1;
                setSelectedForStars(0);
                setStarLayout(0, star[0].isSelected());
            }
        });

        star[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Alert", "star1 clicked");

                currentRating = 2;
                setSelectedForStars(1);
                setStarLayout(1, star[1].isSelected());
            }
        });

        star[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Alert", "star2 clicked");

                currentRating = 3;
                setSelectedForStars(2);
                setStarLayout(2, star[2].isSelected());
            }
        });

        star[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Alert", "star3 clicked");

                currentRating = 4;
                setSelectedForStars(3);
                setStarLayout(3, star[3].isSelected());
            }
        });

        star[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Alert", "star4 clicked");

                currentRating = 5;
                setSelectedForStars(4);
                setStarLayout(4, star[4].isSelected());
            }
        });

        builder.setView(view);

        builder.setTitle("Rate us")
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {

                                sendRatingComment();

                            }
                        }).show();


        try {
            builder.show();
        } catch (Exception e) {

        }

    }

    private void sendRatingComment() {
        JSONObject  internalJsonObj = inputJsonForFeedback();
        String      url             = Constants.BASE_URL + Constants.UPDATE_FEEDBACK;

        Log.e(TAG,"internalJsonObj = "+internalJsonObj+"url:"+url);

        Response.Listener<JSONObject> resListener = new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                String responseFromServer = "";
                String error_message = "";
                String error = "";

                Log.e(TAG,"Response json : "+response);
                if(response!=null) {
                    try {
                        responseFromServer = response.getString("status");
                        error_message  = response.optString("error_message");
                        error = response.optString("error");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Utility.showToast(LinguaConnect.this, "Thank you.");
                }
            }
        };

        //it will pass as parameter in JsonObjectRequest constructor
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG,"onerrorresponse:"+volleyError);

            }
        };

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, internalJsonObj, resListener, errorListener) {

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                    VolleyError error = new VolleyError(new String(volleyError.networkResponse.data));
                    volleyError = error;
                }
                return volleyError;
            }
        };

        DefaultRetryPolicy policy = new DefaultRetryPolicy(2 * 60 * 1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjReq.setRetryPolicy(policy);

        AppController.getInstance().addToRequestQueue(jsonObjReq);

    }

    private JSONObject inputJsonForFeedback() {
        JSONObject inputJson = new JSONObject();

        try {

            inputJson.put(getString(R.string.booking_id), bookingId);
            inputJson.putOpt(getString(R.string.rating),currentRating);
            inputJson.putOpt(getString(R.string.type),"client");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return inputJson;
    }

    void setSelectedForStars(int position){

        for(int i =0; i < star.length; i++){

            if(i == position){
                star[i].setSelected(true);
            }else {
                star[i].setSelected(false);
            }
        }
    }

    void setStarLayout(int position, boolean isSelected){

        if(isSelected) {

            for(int i = 0; i <= position; i++){
                star[i].setImageResource(R.mipmap.ic_star_black);
            }

            for(int i = position + 1; i < star.length; i++){
                star[i].setImageResource(R.mipmap.ic_star_border_black);
            }

        }else {
            Log.e(TAG,"else of setStarLayout");
        }
    }

    private void getCurrentBooking() {
        findViewById(R.id.current_booking).setVisibility(View.VISIBLE);
        JSONObject registerJSON = createJSONForBooking();
        String url = Constants.BASE_URL + Constants.GET_BOOKING;

        Log.e(TAG,url+","+registerJSON);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, registerJSON,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG,"response:"+response);
                        JSONObject data = response.optJSONObject("data");
                        if(data != null) {
                            tvCurrentBooking.setVisibility(View.VISIBLE);
                            tvCurrentBooking.setText(data.optString("interpreter_first_name") +" "+
                                    data.optString("interpreter_last_name") +" has been allocated as your interpreter.");
                            btChangeBooking.setVisibility(View.VISIBLE);
                        }


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
                    Utility.showToast(LinguaConnect.this, responseString.optString("error"));
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

    private void fetchEvent() {
        String url = Constants.BASE_URL + Constants.GET_EVENT_DETAILS;

        Log.e(TAG,url);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG,"response:"+response);

                        JSONArray eventArray = response.optJSONArray("events");
                        if(eventArray != null) {
                            JSONObject eventObject = eventArray.optJSONObject(0);
                            if(eventObject != null) {
                                tvEventName.setText(eventObject.optString("name"));
                                tvEventLocation.setText(eventObject.optString("venue"));
                                tvEventTiming.setText(eventObject.optString("timing"));
                                tvEventDescription.setText(eventObject.optString("description"));

                                ImageLoader imageLoader = AppController.getInstance().getImageLoader();
                                imageLoader.get(eventObject.optString("picture"),
                                        ImageLoader.getImageListener(ivEventImage,R.mipmap.profile,R.mipmap.profile));


                                eventId = eventObject.optString("id");
                            }
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                VolleyError error = null;
                if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                    error = new VolleyError(new String(volleyError.networkResponse.data));
                    try {
                        JSONObject responseString = new JSONObject(error.getMessage());
                        Utility.showToast(LinguaConnect.this, responseString.optString("error"));
                    } catch (JSONException e) {
                        Log.e(TAG,"Exception:"+e);
                        e.printStackTrace();
                    }
                    //Log.e(TAG,"error:"+error+", volleyError:"+volleyError);
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

    private void initScreen() {
        tvCurrentBooking = (TextView) findViewById(R.id.current_booking_text);
        btChangeBooking = (Button) findViewById(R.id.change_booking);
        tvEventName = (TextView) findViewById(R.id.event_name);
        tvEventLocation = (TextView) findViewById(R.id.event_location);
        tvEventTiming = (TextView) findViewById(R.id.event_timing);
        tvEventDescription = (TextView) findViewById(R.id.event_description);
        tvCurrentBooking = (TextView) findViewById(R.id.current_booking_text);
        ivEventImage = (ImageView) findViewById(R.id.event_image);
        counter = (Chronometer) findViewById(R.id.chronometer);
        counter.setBase(SystemClock.elapsedRealtime());
    }

    private JSONObject createJSONForBooking() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(getString(R.string.booking_id), Integer.parseInt(bookingId));
            jsonObject.put("app-version", BuildConfig.VERSION_CODE);
            jsonObject.put("app-version-name",BuildConfig.VERSION_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private boolean isLoginMade() {
        if(Utility.getLocalBoolean(this, Constants.IS_LOGGED_IN)) {
            return true;
        } else {
            Intent intent = new Intent(this,LoginActivity.class);
            startActivity(intent);
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        if (isDrawerOpened) {
            drawerFragment.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDrawerOpen() {
        isDrawerOpened = false;
    }

    @Override
    public void onDrawerClose() {
        isDrawerOpened = true;
    }

    public void changeCurrentBooking(View view) {
        Log.e(TAG,"isbookingReceived on click:"+isBookingReceived);
        updateCurrentBookingStatus(isBookingReceived);
    }

    private void updateCurrentBookingStatus(final boolean isBookingReceived) {
        final HandleProgressBar progress = new HandleProgressBar(this);
        if(progress != null) {
            progress.showProgressBar();
        }
        JSONObject registerJSON = createJSONForUpdateBooking(isBookingReceived);
        String url = Constants.BASE_URL + Constants.UPDATE_CURRENT_BOOKING;

        Log.e(TAG,url+","+registerJSON);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, registerJSON,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        if(progress != null) {
                            progress.hideProgressBar();
                        }
                        Log.e(TAG,"response:"+response);
                        if(isBookingReceived) {
                            LinguaConnect.isBookingReceived = false;
                            if(counter != null) {
                                counter.setBase(SystemClock.elapsedRealtime());
                                counter.start();
                                btChangeBooking.setText("End Booking");
                                Log.e(TAG,"button text changed");
                            }
                        } else {
                            findViewById(R.id.current_booking).setVisibility(View.GONE);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if(progress != null) {
                    progress.hideProgressBar();
                }
                VolleyError error = null;
                if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                    error = new VolleyError(new String(volleyError.networkResponse.data));
                    //Log.e(TAG,"error:"+error+", volleyError:"+volleyError);
                }
                try {
                    JSONObject responseString = new JSONObject(error.getMessage());
                    Utility.showToast(LinguaConnect.this, responseString.optString("error"));
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

    private JSONObject createJSONForUpdateBooking(boolean isBookingReceived) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(getString(R.string.booking_id), Integer.parseInt(bookingId));
            jsonObject.put("app-version", BuildConfig.VERSION_CODE);
            jsonObject.put("app-version-name",BuildConfig.VERSION_NAME);
            if(isBookingReceived) {
                jsonObject.put("type", "start");
            } else {
                jsonObject.put("type", "end");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
