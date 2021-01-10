package com.example.servicefinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditDescriptionActivity extends AppCompatActivity {

    private TextInputLayout LayoutServiceProviderInfo, LayoutSpecialityProviderInfo, LayoutPhoneNumberProviderInfo, LayoutDescriptionProviderInfo;
    private TextInputEditText txtEditServiceProviderInfo, txtEditSpecialityProviderInfo, txtEditPhoneNumberProviderInfo, txtEditDescriptionProviderInfo;
    private Button btnSave;
    private SharedPreferences userPref;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_description);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarHome);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        init();
    }

    private void init() {
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        LayoutServiceProviderInfo = findViewById(R.id.LayoutServiceProviderInfo);
        LayoutSpecialityProviderInfo = findViewById(R.id.LayoutSpecialityProviderInfo);
        LayoutPhoneNumberProviderInfo = findViewById(R.id.LayoutPhoneNumberProviderInfo);
        LayoutDescriptionProviderInfo = findViewById(R.id.LayoutDescriptionProviderInfo);

        txtEditServiceProviderInfo = findViewById(R.id.txtEditServiceProviderInfo);
        txtEditSpecialityProviderInfo = findViewById(R.id.txtEditSpecialityProviderInfo);
        txtEditPhoneNumberProviderInfo = findViewById(R.id.txtEditPhoneNumberProviderInfo);
        txtEditDescriptionProviderInfo = findViewById(R.id.txtEditDescriptionProviderInfo);

        btnSave = findViewById(R.id.btnEditSave);

        if(userPref.getString("service","").equals("null")){
            txtEditServiceProviderInfo.setText("");
        }
        else{
            txtEditServiceProviderInfo.setText(userPref.getString("service",""));
        }
        if(userPref.getString("speciality","").equals("null")){
            txtEditSpecialityProviderInfo.setText("");

        }
        else{
            txtEditSpecialityProviderInfo.setText(userPref.getString("speciality",""));
        }
        if(userPref.getString("phone_number","").equals("null")){
            txtEditPhoneNumberProviderInfo.setText("");
        }
        else{
            txtEditPhoneNumberProviderInfo.setText(userPref.getString("phone_number",""));
        }
        if(userPref.getString("description","").equals("null")){
            txtEditDescriptionProviderInfo.setText("");
        }
        else{
            txtEditDescriptionProviderInfo.setText(userPref.getString("description",""));

        }
        btnSave.setOnClickListener(v->{
            if (validate()){
                editDescription();
            }
        });

    }

    private void editDescription() {

        dialog.setMessage("Updating");
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST,Constant.SAVE_USER_INFO, res->{

            try {
                JSONObject object = new JSONObject(res);
                if (object.getBoolean("success")){
                    SharedPreferences.Editor editor = userPref.edit();
                    editor.putString("service",txtEditServiceProviderInfo.getText().toString().trim());
                    editor.putString("speciality",txtEditSpecialityProviderInfo.getText().toString().trim());
                    editor.putString("phone_number",txtEditPhoneNumberProviderInfo.getText().toString().trim());
                    editor.putString("description",txtEditDescriptionProviderInfo.getText().toString().trim());
                    editor.apply();
                    Toast.makeText(this, "Description Updated!", Toast.LENGTH_LONG).show();
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dialog.dismiss();
        },err->{
            err.printStackTrace();
            dialog.dismiss();
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = userPref.getString("token","");
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization","Bearer "+token);
                return map;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("service",txtEditServiceProviderInfo.getText().toString().trim());
                map.put("speciality",txtEditSpecialityProviderInfo.getText().toString().trim());
                map.put("phone_number",txtEditPhoneNumberProviderInfo.getText().toString().trim());
                map.put("description",txtEditDescriptionProviderInfo.getText().toString().trim());
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(EditDescriptionActivity.this);
        queue.add(request);

    }

    private boolean validate() {
        if (txtEditServiceProviderInfo.getText().toString().isEmpty()){
            LayoutServiceProviderInfo.setErrorEnabled(true);
            LayoutServiceProviderInfo.setError("Service is required!");
            return false;
        }
        if (txtEditSpecialityProviderInfo.getText().toString().isEmpty()){
            LayoutSpecialityProviderInfo.setErrorEnabled(true);
            LayoutSpecialityProviderInfo.setError("Speciality is required!");
            return false;
        }
        if (txtEditPhoneNumberProviderInfo.getText().toString().isEmpty()){
            LayoutPhoneNumberProviderInfo.setErrorEnabled(true);
            LayoutPhoneNumberProviderInfo.setError("Phone number is required!");
            return false;
        }
        if (txtEditDescriptionProviderInfo.getText().toString().isEmpty()){
            LayoutDescriptionProviderInfo.setErrorEnabled(true);
            LayoutDescriptionProviderInfo.setError("Description is required!");
            return false;
        }

        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}