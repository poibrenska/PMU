package com.pmu.gift_app.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pmu.gift_app.R;
import com.pmu.gift_app.app.AppConfig;
import com.pmu.gift_app.app.AppController;
import com.pmu.gift_app.helper.SQLiteHandler;
import com.pmu.gift_app.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eleonora Poibrenska on 12.09.2020
 */

public class CreateEventActivity extends Activity {
    private static final String TAG = CreateEventActivity.class.getSimpleName();
    private SQLiteHandler db;
    private SessionManager session;
    private ProgressDialog pDialog;
    private Button btnBack;
    private Button btnSave;
    private EditText inputName;
    private EditText inputAddress;
    private EditText inputDescription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        btnBack = (Button) findViewById(R.id.btnBack);
        btnSave = (Button) findViewById(R.id.btnSave);
        inputName = (EditText) findViewById(R.id.txtName);
        inputAddress = (EditText) findViewById(R.id.txtAddress);
        inputDescription = (EditText) findViewById(R.id.txtDescription);


        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        btnBack.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        MainActivity.class);
                startActivity(i);
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String name = inputName.getText().toString().trim();
                String address = inputAddress.getText().toString().trim();
                String description = inputDescription.getText().toString().trim();

                if (!name.isEmpty() && !address.isEmpty() && !description.isEmpty()) {
                    //send reques to the server
                    createEvent(name, address, description);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please fill all the fileds!", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });
    }

    private void createEvent(final String name, final String address,
                             final String description) {
        // Tag used to cancel the request
        String tag_string_req = "req_create_event";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_CREATE_EVENT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    System.out.println(response);
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // event successfully stored in MySQL
                        // Now store the event in sqlite
                        JSONObject event = jObj.getJSONObject("event");
                        Integer id = event.getInt("id");
                        String code = event.getString("code");
                        String name = event.getString("name");
                        String address = event.getString("address");
                        String description = event.getString("description");
                        String userId = event.getString("user_id");

                        Intent intent = new Intent(
                                CreateEventActivity.this,
                                MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Save Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                HashMap<String, String> user = db.getUserDetails();
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
                params.put("address", address);
                params.put("description", description);
                params.put("user_id", user.get("uid"));
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

}