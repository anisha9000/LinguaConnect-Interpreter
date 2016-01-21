package interpreter.linguaconnect.com.linguaconnectinterpreter.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.appcompat.BuildConfig;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import interpreter.linguaconnect.com.linguaconnectinterpreter.AppController;
import interpreter.linguaconnect.com.linguaconnectinterpreter.R;
import interpreter.linguaconnect.com.linguaconnectinterpreter.utils.Constants;
import interpreter.linguaconnect.com.linguaconnectinterpreter.utils.Utility;

public class UpdatePassword extends AppCompatActivity {

    private String TAG = UpdatePassword.class.getName();
    EditText etConfirmPassword;
    EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        initScreen();
    }

    private void initScreen() {
        etConfirmPassword = (EditText) findViewById(R.id.confirm_password);
        etPassword = (EditText) findViewById(R.id.password);
    }

    public void changePassword(View view) {

        if(validatePassword()) {
            JSONObject registerJSON = createJSONForLogin();

            String url = Constants.BASE_URL + Constants.UPDATE_PASSWORD;

            Log.e(TAG,registerJSON+","+url);

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    url, registerJSON,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e(TAG,"response:"+response);
                            Utility.showToast(UpdatePassword.this,"Password changed successfully");
                            startActivity(new Intent(UpdatePassword.this, EditProfile.class));
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
                        Log.e(TAG,"");
                        Utility.showToast(UpdatePassword.this, responseString.optString("error"));
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
    }

    private boolean validatePassword() {
        if(etPassword.getText().toString().equals(etConfirmPassword.getText().toString())) {
            return true;
        } else {
            Utility.showToast(this,"Password mismatch.");
            return false;
        }
    }

    private JSONObject createJSONForLogin() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(getString(R.string.email), Utility.getLocalString(this, Constants.USER_EMAIL));
            jsonObject.put(getString(R.string.password), etPassword.getText().toString());
            jsonObject.put("app-version", BuildConfig.VERSION_CODE);
            jsonObject.put("app-version-name",BuildConfig.VERSION_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
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
