package com.pmu.gift_app.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.pmu.gift_app.helper.SQLiteHandler;
import com.pmu.gift_app.helper.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eleonora Poibrenska on 12.09.2020
 */

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView txtName;
    private TextView txtEmail;
    private Button btnLogout;
    private Button btnCreate;
    private Button btnSearch;
    private EditText txtSearch;
    private SQLiteHandler db;
    private SessionManager session;
    private ProgressDialog pDialog;
    Button eventButton;
    ScrollView scrollview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtName = (TextView) findViewById(R.id.name);
        txtEmail = (TextView) findViewById(R.id.email);
        txtSearch = (EditText) findViewById(R.id.txtSearch);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnCreate = (Button) findViewById(R.id.btnCreate);
        btnSearch = (Button) findViewById(R.id.btnSearch);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        } else {
            final HashMap<String, String> user = db.getUserDetails();
            loadEvents(user.get("uid"));
        }

        // Fetching user details from SQLite
        final HashMap<String, String> user = db.getUserDetails();

        String name = user.get("name");
        String email = user.get("email");

        // Displaying the user details on the screen
        txtName.setText(name);
        txtEmail.setText(email);

        // Logout button click event
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        // Link to Create Screen
        btnCreate.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        CreateEventActivity.class);
                startActivity(i);
                finish();
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String code = txtSearch.getText().toString().trim();
                if (!code.isEmpty()) {
                    //send reques to the server
                    searchEvent(code, user.get("uid"));
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please fill the search filed!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

    }
    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void searchEvent(final String code, final String user_id) {
        // Tag used to cancel the request
        String tag_string_req = "req_search_event";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_SEARCH_EVENT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        //open event
                        JSONObject event = jObj.getJSONObject("event");
                        String userId = event.getString("user_id");
                        if(userId.equals(user_id)) {
                            Intent intent = new Intent(
                                    MainActivity.this,
                                    MyEventInfoActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("response", event.toString());
                            intent.putExtras(bundle);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent(
                                    MainActivity.this,
                                    EventInfoActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("response", event.toString());
                            intent.putExtras(bundle);
                            startActivity(intent);
                            finish();
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

    private void loadEvents(final String user_id){
        scrollview = (ScrollView)findViewById(R.id.scrollBottomButtons);
        final LinearLayout linearlayout = new LinearLayout(this);
        linearlayout.setOrientation(LinearLayout.VERTICAL);
        scrollview.addView(linearlayout);

        String tag_string_req = "req_load_events";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_GET_EVENTS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        //open event
                        JSONArray events = jObj.getJSONArray("events");
                        for(int i = 0; i < events.length(); i++)
                        {
                            JSONObject event = events.getJSONObject(i);
                            createButton(event, linearlayout);
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
                params.put("user_id", user_id);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

    }

    private void createButton(final JSONObject event, LinearLayout linearlayout){
        LinearLayout linear1 = new LinearLayout(this);
        linear1.setOrientation(LinearLayout.HORIZONTAL);
        linearlayout.addView(linear1);
        eventButton = new Button(this);
        try {
            eventButton.setText(event.getString("name"));
            eventButton.setId(event.getInt("id"));
        }  catch (JSONException e) {
            e.printStackTrace();
        }
        eventButton.setTextSize(12);
        eventButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        linear1.addView(eventButton);


        eventButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent intent = new Intent(
                        MainActivity.this,
                        MyEventInfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("response", event.toString());
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });
    }
}