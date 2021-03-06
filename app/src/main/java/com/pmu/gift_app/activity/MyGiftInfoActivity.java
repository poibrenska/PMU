package com.pmu.gift_app.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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

public class MyGiftInfoActivity extends Activity {
    private static final String TAG = MyGiftInfoActivity.class.getSimpleName();
    private Button btnDelete;
    private Button btnBack;
    private TextView txtName;
    private TextView txtDescription;
    private TextView lblBookedGift;
    private ProgressDialog pDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_gift_info);

        btnBack = (Button) findViewById(R.id.btnBack);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        txtName = (TextView) findViewById(R.id.txtName);
        txtDescription = (TextView) findViewById(R.id.txtDescription);
        lblBookedGift = (TextView) findViewById(R.id.lblBookedGift);

        final Bundle bundle = getIntent().getExtras();
        String response = bundle.getString("gift");
        Integer id = null;
        String user_id = null;
        try {
            JSONObject gift = new JSONObject(response);
            id = gift.getInt("id");
            String name = gift.getString("name");
            String description = gift.getString("description");
            user_id = gift.getString("user_id");
            txtName.setText(name);
            txtDescription.setText(description);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(user_id.equalsIgnoreCase("null") || user_id == null || user_id.isEmpty()){
            lblBookedGift.setVisibility(View.INVISIBLE);

        } else {
            btnDelete.setVisibility(View.INVISIBLE);
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        MyEventInfoActivity.class);
                i.putExtras(bundle);
                startActivity(i);
                finish();
            }
        });

        final Integer gift_id = id;

        btnDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                deleteGift(gift_id, bundle);
            }
        });

    }

    private void deleteGift(final Integer id, final Bundle bundle) {
        // Tag used to cancel the request
        String tag_string_req = "req_delete_gift";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_DELETE_GIFT, new Response.Listener<String>() {

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
                                MyGiftInfoActivity.this,
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
                Log.e(TAG, "Delete Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id.toString());
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
