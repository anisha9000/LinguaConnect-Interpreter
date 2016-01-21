package interpreter.linguaconnect.com.linguaconnectinterpreter.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import interpreter.linguaconnect.com.linguaconnectinterpreter.AppController;
import interpreter.linguaconnect.com.linguaconnectinterpreter.R;
import interpreter.linguaconnect.com.linguaconnectinterpreter.ui.adapter.HistoryAdapter;
import interpreter.linguaconnect.com.linguaconnectinterpreter.ui.adapter.HistoryItem;
import interpreter.linguaconnect.com.linguaconnectinterpreter.utils.Constants;
import interpreter.linguaconnect.com.linguaconnectinterpreter.utils.Utility;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    String eventId;
    private String TAG = HistoryActivity.class.getName();
    private HistoryAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        initScreen();
        fetchHistory();
    }

    private void initScreen() {
        mRecyclerView = (RecyclerView) findViewById(R.id.history_list_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    private void fetchHistory() {
        String url = Constants.BASE_URL + Constants.GET_HISTORY + "?" + getString(R.string.email)
                + "="+Utility.getLocalString(this, Constants.USER_EMAIL);

        Log.e(TAG, url);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG,"response:"+response);

                        JSONArray historyArray = response.optJSONArray("data");
                        ArrayList<HistoryItem> responseList = new ArrayList<>();
                        if(historyArray != null) {
                            for (int i = 0; i < historyArray.length(); i++) {
                                JSONObject historyObject = historyArray.optJSONObject(i);
                                if(historyObject != null) {

                                    responseList.add(new HistoryItem(
                                            historyObject.optString(getString(R.string.client__user__first_name)),
                                            historyObject.optString(getString(R.string.client__user__last_name)),
                                            historyObject.optString(getString(R.string.language)),
                                            historyObject.optString(getString(R.string.start_time)),
                                            historyObject.optString(getString(R.string.end_time)),
                                            historyObject.optString(getString(R.string.client__picture_url)),
                                            historyObject.optInt(getString(R.string.client_rating)),
                                            historyObject.optString("status")
                                    ));

                                    mAdapter = new HistoryAdapter(responseList, HistoryActivity.this);
                                    mRecyclerView.setVisibility(View.VISIBLE);
                                    mRecyclerView.setAdapter(mAdapter);
                                }

                            }
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                VolleyError error = null;
                if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                    error = new VolleyError(new String(volleyError.networkResponse.data));
                    Log.e(TAG,"error:"+error+", volleyError:"+volleyError);
                }
                try {
                    JSONObject responseString = new JSONObject(error.getMessage());
                    Utility.showToast(HistoryActivity.this, responseString.optString("error"));
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
