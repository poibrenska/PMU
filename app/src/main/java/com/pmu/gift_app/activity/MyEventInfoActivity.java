package com.pmu.gift_app.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pmu.gift_app.R;
import com.pmu.gift_app.app.AppConfig;
import com.pmu.gift_app.app.AppController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eleonora Poibrenska on 12.09.2020
 */

public class MyEventInfoActivity extends Activity {
    private static final String TAG = MyEventInfoActivity.class.getSimpleName();
    private Button btnBack;
    private Button btnDelete;
    private Button btnAddGift;
    private TextView txtCode;
    private TextView txtName;
    private TextView txtAddress;
    private TextView txtDescription;
    private ProgressDialog pDialog;
    Button giftButton;
    ScrollView scrollview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_event_info);

        btnBack = (Button) findViewById(R.id.btnBack);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        btnAddGift = (Button) findViewById(R.id.btnAddGift);
        txtCode = (TextView) findViewById(R.id.txtCode);
        txtName = (TextView) findViewById(R.id.txtName);
        txtAddress = (TextView) findViewById(R.id.txtAddress);
        txtDescription = (TextView) findViewById(R.id.txtDescription);

        final Bundle bundle = getIntent().getExtras();
        String response = bundle.getString("response");
        String code = null;
        try {
            JSONObject event = new JSONObject(response);
            Integer id = event.getInt("id");
            code = event.getString("unique_code");
            String name = event.getString("name");
            String address = event.getString("address");
            String description = event.getString("description");
            String userId = event.getString("user_id");

            txtCode.setText(code);
            txtName.setText(name);
            txtAddress.setText(address);
            txtDescription.setText(description);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        loadGifts(code, bundle);
        btnBack.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        MainActivity.class);
                startActivity(i);
                finish();
            }
        });

        btnAddGift.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        CreateGiftActivity.class);
                i.putExtras(bundle);
                startActivity(i);
                finish();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                deleteEvent( txtCode.getText().toString().trim());
            }
        });
    }

    private void deleteEvent(final String code) {
        // Tag used to cancel the request
        String tag_string_req = "req_delete_event";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_DELETE_EVENT, new Response.Listener<String>() {

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
                                MyEventInfoActivity.this,
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
                Log.e(TAG, "Delete Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("unique_code", code);
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

    private void loadGifts(final String event_id, final Bundle bundle){
        scrollview = (ScrollView)findViewById(R.id.scrollGiftButtons);
        final LinearLayout linearlayout = new LinearLayout(this);
        linearlayout.setOrientation(LinearLayout.VERTICAL);
        scrollview.addView(linearlayout);

        String tag_string_req = "req_load_gifts";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_GET_GIFTS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        //open event
                        JSONArray gifts = jObj.getJSONArray("gifts");
                        for(int i = 0; i < gifts.length(); i++)
                        {
                            JSONObject event = gifts.getJSONObject(i);
                            createButton(event, linearlayout, bundle);
                        }

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
                Map<String, String> params = new HashMap<String, String>();
                params.put("event_id", event_id);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

    }

    private void createButton(final JSONObject gift, LinearLayout linearlayout, final Bundle bundle){
        LinearLayout linear1 = new LinearLayout(this);
        linear1.setOrientation(LinearLayout.HORIZONTAL);
        linearlayout.addView(linear1);
        giftButton = new Button(this);
        try {
            giftButton.setText(gift.getString("name"));
            giftButton.setId(gift.getInt("id"));
            String user_id = (gift.getString("user_id"));
            if(user_id.equalsIgnoreCase("null") || user_id == null || user_id.isEmpty()){

            } else {
                giftButton.setBackgroundColor(0x4F64FF61);
            }
        }  catch (JSONException e) {
            e.printStackTrace();
        }
        giftButton.setTextSize(12);
        giftButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        linear1.addView(giftButton);


        giftButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent intent = new Intent(
                        MyEventInfoActivity.this,
                        MyGiftInfoActivity.class);
                bundle.putString("gift", gift.toString());
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });
    }
}
