package com.example.servicefinder;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.servicefinder.Adapters.PostsAdapter;
import com.example.servicefinder.Fragments.SignUpFragment;
import com.example.servicefinder.Models.Post;
import com.example.servicefinder.Models.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //this code will pause the app for 1.5 secs and then any thing in run method will run.
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            SharedPreferences userPref = getApplicationContext().getSharedPreferences("user",Context.MODE_PRIVATE);
            StringRequest request = new StringRequest(Request.Method.POST, Constant.VERIFY_USER, response -> {
                try {
                    JSONObject object = new JSONObject(response);
                    SharedPreferences.Editor editor = userPref.edit();
                    editor.putBoolean("isLoggedIn", object.getBoolean("success"));
                    editor.apply();
                }
                 catch (JSONException e) {
                    e.printStackTrace();
                }


            },error -> {
                error.printStackTrace();
            }){

                // provide token in header

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    String token = userPref.getString("token","");
                    HashMap<String,String> map = new HashMap<>();
                    map.put("Authorization","Bearer "+token);
                    return map;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            queue.add(request);
            boolean isLoggedIn = userPref.getBoolean("isLoggedIn", false);
            //isLoggedIn = false; // delete this when logout done
            if (isLoggedIn){
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
            }
            else {
                startActivity(new Intent(MainActivity.this, AuthActivity.class));
            }
            finish();
        }, 500);
    }

    }



