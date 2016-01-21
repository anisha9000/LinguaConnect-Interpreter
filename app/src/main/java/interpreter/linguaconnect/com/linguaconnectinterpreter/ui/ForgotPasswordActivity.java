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
import interpreter.linguaconnect.com.linguaconnectinterpreter.utils.Utility;

public class ForgotPasswordActivity extends AppCompatActivity {

    private String TAG = ForgotPasswordActivity.class.getName();

    EditText etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initScreen();
    }

    private void initScreen() {
        etEmail = (EditText) findViewById(R.id.email);
    }

    public void requestPassword(View view) {
        JSONObject registerJSON = createJSONForPassword();

        String url = Constants.BASE_URL + Constants.FORGOT_PASSWORD;

        Log.e(TAG,registerJSON+","+url);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, registerJSON,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG,"response:"+response);
                        Utility.showToast(ForgotPasswordActivity.this, response.optString("message"));
                        startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
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
                    JSONObject responseString = null;
                    if (error != null) {
                        responseString = new JSONObject(error.getMessage());
                    }
                    Log.e(TAG,"");
                    Utility.showToast(ForgotPasswordActivity.this, responseString.optString("error"));
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

    private JSONObject createJSONForPassword() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(getString(R.string.email), etEmail.getText().toString());
            jsonObject.put("app-version", BuildConfig.VERSION_CODE);
            jsonObject.put("app-version-name",BuildConfig.VERSION_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
