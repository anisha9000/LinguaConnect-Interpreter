package interpreter.linguaconnect.com.linguaconnectinterpreter.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import org.json.JSONException;
import org.json.JSONObject;

import interpreter.linguaconnect.com.linguaconnectinterpreter.AppController;
import interpreter.linguaconnect.com.linguaconnectinterpreter.BuildConfig;
import interpreter.linguaconnect.com.linguaconnectinterpreter.R;
import interpreter.linguaconnect.com.linguaconnectinterpreter.utils.Constants;
import interpreter.linguaconnect.com.linguaconnectinterpreter.utils.Utility;

public class EditProfile extends AppCompatActivity {

    EditText etFirstName, etLastName, etPhone, etEmail;
    EditText etAge;
    RadioGroup rgGender;
    RadioButton rbMale, rbFemale;
    String genderSelected;
    TextView updatePassword;
    private String TAG = EditProfile.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        initScreen();
        setProfileElements();
    }

    private void setProfileElements() {
        etFirstName.setText(Utility.getLocalString(this, Constants.USER_FIRST_NAME));
        etLastName.setText(Utility.getLocalString(this,Constants.USER_LAST_NAME));
        etPhone.setText(Utility.getLocalString(this,Constants.USER_PHONE_NUMBER));
        etEmail.setText(Utility.getLocalString(this,Constants.USER_EMAIL));
        etAge.setText(String.valueOf(Utility.getLocalInt(this,Constants.USER_AGE)));
        if(Utility.getLocalString(this,Constants.USER_GENDER).equalsIgnoreCase("male")) {
            rbFemale.setSelected(true);
        } else if(Utility.getLocalString(this,Constants.USER_GENDER).equalsIgnoreCase("female")) {
            rbMale.setSelected(true);
        }
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                etLocation.setText(place.getName());
                Log.e(TAG, "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.e(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }*/

    private void initScreen() {
        etFirstName = (EditText) findViewById(R.id.first_name);
        etLastName = (EditText) findViewById(R.id.last_name);
        etPhone = (EditText) findViewById(R.id.avatar_contact);
        etEmail = (EditText) findViewById(R.id.avatar_email);
        etAge = (EditText) findViewById(R.id.avatar_age);
        rgGender = (RadioGroup) findViewById(R.id.avatar_gender);
        rbFemale = (RadioButton) findViewById(R.id.radio_female);
        rbMale = (RadioButton) findViewById(R.id.radio_male);
        updatePassword = (TextView) findViewById(R.id.update_password);
        genderSelected = "male";

        rgGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_male:
                        genderSelected = "male";
                        break;
                    case R.id.radio_female:
                        genderSelected = "female";
                        break;
                }
            }
        });

        updatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditProfile.this, UpdatePassword.class));
            }
        });
    }

    public void saveProfile(View view) {
        final JSONObject registerJSON = createJSONForEdit();

        String url = Constants.BASE_URL + Constants.UPDATE_CLIENT;

        Log.e(TAG,registerJSON+","+url);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, registerJSON,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG,"response:"+response);
                        updateLocalData(registerJSON);
                        Utility.showToast(EditProfile.this,response.optString("message"));
                        finish();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                VolleyError error = null;
                if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                    error = new VolleyError(new String(volleyError.networkResponse.data));
                }
                try {
                    JSONObject responseString = new JSONObject(error.getMessage());
                    Utility.showToast(EditProfile.this, responseString.optString("error"));
                    if(volleyError.networkResponse.statusCode == 500) {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e(TAG, ""+volleyError.getMessage());
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

    private void updateLocalData (JSONObject jsonObject) {
        Utility.saveLocalString(this,Constants.USER_FIRST_NAME, jsonObject.optString(getString(R.string.first_name)));
        Utility.saveLocalString(this,Constants.USER_LAST_NAME, jsonObject.optString(getString(R.string.last_name)));
        Utility.saveLocalString(this,Constants.USER_PHONE_NUMBER, jsonObject.optString(getString(R.string.phone_number)));
        Utility.saveLocalString(this,Constants.USER_EMAIL, jsonObject.optString(getString(R.string.email)));
        Utility.saveLocalInt(this,Constants.USER_AGE, Integer.parseInt(jsonObject.optString(getString(R.string.age))));
        Utility.saveLocalString(this,Constants.USER_GENDER, jsonObject.optString(getString(R.string.gender)));
    }

    private JSONObject createJSONForEdit() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(getString(R.string.email), etEmail.getText().toString());
            jsonObject.put(getString(R.string.first_name), etFirstName.getText().toString());
            jsonObject.put(getString(R.string.last_name), etLastName.getText().toString());
            jsonObject.put(getString(R.string.age), etAge.getText().toString());
            jsonObject.put(getString(R.string.gender), genderSelected);
            jsonObject.put(getString(R.string.phone_number), etPhone.getText().toString());
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