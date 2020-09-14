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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eleonora Poibrenska on 12.09.2020
 */

public class CreateGiftActivity extends Activity {
    private static final String TAG = CreateGiftActivity.class.getSimpleName();
    private Button btnBack;
    private Button btnSave;
    private EditText inputName;
    private EditText inputDescription;
    private ProgressDialog pDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_gift);
        final Bundle bundle = getIntent().getExtras();

        btnBack = (Button) findViewById(R.id.btnBack);
        btnSave = (Button) findViewById(R.id.btnSave);
        inputName = (EditText) findViewById(R.id.txtName);
        inputDescription = (EditText) findViewById(R.id.txtDescription);

        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        MyEventInfoActivity.class);
                i.putExtras(bundle);
                startActivity(i);
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String name = inputName.getText().toString().trim();
                String description = inputDescription.getText().toString().trim();

                if (!name.isEmpty() && !description.isEmpty()) {
                    //send reques to the server
                    createGift(name,description,bundle);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please fill all the fileds!", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

    }
    private void createGift(final String name, final String description, final Bundle bundle) {
        // Tag used to cancel the request
        String tag_string_req = "req_create_gift";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_CREATE_GIFT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        String errorMsg = jObj.getString("msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(
                                CreateGiftActivity.this,
                                MyEventInfoActivity.class);
                        intent.putExtras(bundle);
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
                String response = bundle.getString("response");
                String code = null;
                try {
                    JSONObject event = new JSONObject(response);
                    code = event.getString("unique_code");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("event_id", code);
                params.put("name", name);
                params.put("description", description);
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
