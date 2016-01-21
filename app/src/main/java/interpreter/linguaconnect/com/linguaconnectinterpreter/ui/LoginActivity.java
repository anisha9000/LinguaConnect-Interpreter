package interpreter.linguaconnect.com.linguaconnectinterpreter.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import interpreter.linguaconnect.com.linguaconnectinterpreter.BuildConfig;
import interpreter.linguaconnect.com.linguaconnectinterpreter.R;
import interpreter.linguaconnect.com.linguaconnectinterpreter.utils.Constants;
import interpreter.linguaconnect.com.linguaconnectinterpreter.utils.GCMUtils;
import interpreter.linguaconnect.com.linguaconnectinterpreter.utils.Utility;

public class LoginActivity extends AppCompatActivity {

    private String TAG = LoginActivity.class.getName();

    EditText etEmail;
    EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.e(TAG,"oncreate");
        initScreen();
    }

    private void initScreen() {
        etEmail = (EditText) findViewById(R.id.email);
        etPassword = (EditText) findViewById(R.id.password);
    }

    public void loginUser(View view) {
        JSONObject registerJSON = createJSONForLogin();

        String url = Constants.BASE_URL + Constants.LOGIN_URL;

        Log.e(TAG,registerJSON+","+url);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, registerJSON,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG,"response:"+response);

                        Utility.saveLocalBoolean(LoginActivity.this, Constants.IS_LOGGED_IN, true);
                        Utility.saveLocalString(LoginActivity.this, Constants.USER_PHONE_NUMBER, response.optString(getString(R.string.phone_number)));
                        Utility.saveLocalString(LoginActivity.this, Constants.USER_PICTURE_URL, response.optString(getString(R.string.picture_url)));
                        Utility.saveLocalString(LoginActivity.this, Constants.USER_LOCATION, response.optString(getString(R.string.nationality)));
                        Utility.saveLocalString(LoginActivity.this, Constants.USER_LAST_NAME, response.optString(getString(R.string.last_name)));
                        Utility.saveLocalString(LoginActivity.this, Constants.USER_EMAIL, response.optString(getString(R.string.email)));
                        Utility.saveLocalString(LoginActivity.this, Constants.USER_FIRST_NAME, response.optString(getString(R.string.first_name)));
                        Utility.saveLocalInt(LoginActivity.this, Constants.USER_AGE, response.optInt(getString(R.string.age)));
                        Utility.saveLocalString(LoginActivity.this, Constants.USER_GENDER, response.optString(getString(R.string.gender)));
                        Utility.saveLocalInt(LoginActivity.this, Constants.USER_RATING, response.optInt(getString(R.string.rating)));
                        registerWithGCM(LoginActivity.this);
                        startActivity(new Intent(LoginActivity.this, LinguaConnect.class));
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
                    Utility.showToast(LoginActivity.this, responseString.optString("error"));
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

    private void registerWithGCM(LoginActivity context) {
        GCMUtils.issueGCMRegistration(context);
    }

    private JSONObject createJSONForLogin() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(getString(R.string.email), etEmail.getText().toString());
            jsonObject.put(getString(R.string.password), etPassword.getText().toString());
            jsonObject.put("app-version", BuildConfig.VERSION_CODE);
            jsonObject.put("app-version-name",BuildConfig.VERSION_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public void switchToForgotPassword(View view) {
        Intent intent = new Intent(this,ForgotPasswordActivity.class);
        startActivity(intent);
    }
}
