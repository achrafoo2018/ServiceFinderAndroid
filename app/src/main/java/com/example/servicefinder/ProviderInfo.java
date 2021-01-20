package com.example.servicefinder;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProviderInfo extends AppCompatActivity {
    private TextInputLayout LayoutSpecialityProviderInfo, LayoutDescriptionProviderInfo;
    private TextInputEditText txtEditSpecialityProviderInfo, txtEditDescriptionProviderInfo;
    private Button btnSave;
    private SharedPreferences userPref;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_info);
        init();

    }

    private void init() {
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        LayoutSpecialityProviderInfo = findViewById(R.id.LayoutSpecialityProviderInfo);
        LayoutDescriptionProviderInfo = findViewById(R.id.LayoutDescriptionProviderInfo);

        txtEditSpecialityProviderInfo = findViewById(R.id.txtEditSpecialityProviderInfo);
        txtEditDescriptionProviderInfo = findViewById(R.id.txtEditDescriptionProviderInfo);

        btnSave = findViewById(R.id.btnEditSave);
        btnSave.setOnClickListener(v->{
            if (validate()){
                editDescription();
            }
        });

    }

    private void editDescription() {
        {

            dialog.setMessage("Saving...");
            dialog.show();
            StringRequest request = new StringRequest(Request.Method.POST, Constant.SAVE_USER_INFO, res -> {

                try {
                    JSONObject object = new JSONObject(res);
                    if (object.getBoolean("success")) {
                        SharedPreferences.Editor editor = userPref.edit();
                        editor.putString("speciality", txtEditSpecialityProviderInfo.getText().toString().trim());
                        editor.putString("description", txtEditDescriptionProviderInfo.getText().toString().trim());
                        editor.apply();
                        Intent intent = new Intent(ProviderInfo.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }, err -> {
                err.printStackTrace();
                dialog.dismiss();
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    String token = userPref.getString("token", "");
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Authorization", "Bearer " + token);
                    return map;
                }

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("speciality", txtEditSpecialityProviderInfo.getText().toString().trim());
                    map.put("description", txtEditDescriptionProviderInfo.getText().toString().trim());
                    return map;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(ProviderInfo.this);
            queue.add(request);

        }
    }
    private boolean validate() {
        if (txtEditSpecialityProviderInfo.getText().toString().isEmpty()){
            LayoutSpecialityProviderInfo.setErrorEnabled(true);
            LayoutSpecialityProviderInfo.setError("Speciality is required!");
            return false;
        }
        if (txtEditDescriptionProviderInfo.getText().toString().isEmpty()){
            LayoutDescriptionProviderInfo.setErrorEnabled(true);
            LayoutDescriptionProviderInfo.setError("Description is required!");
            return false;
        }

        return true;
    }
    }