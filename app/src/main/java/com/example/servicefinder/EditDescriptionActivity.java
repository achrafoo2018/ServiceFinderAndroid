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
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditDescriptionActivity extends AppCompatActivity {

    private TextInputLayout LayoutSpecialityProviderInfo, LayoutDescriptionProviderInfo;
    private TextInputEditText txtEditSpecialityProviderInfo, txtEditDescriptionProviderInfo;
    private Button btnSave;
    private SharedPreferences userPref;
    private ProgressDialog dialog;
    private SlidrInterface slidr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_description);
        slidr = Slidr.attach(this);
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
        LayoutSpecialityProviderInfo = findViewById(R.id.LayoutSpecialityProviderInfo);
        LayoutDescriptionProviderInfo = findViewById(R.id.LayoutDescriptionProviderInfo);

        txtEditSpecialityProviderInfo = findViewById(R.id.txtEditSpecialityProviderInfo);
        txtEditDescriptionProviderInfo = findViewById(R.id.txtEditDescriptionProviderInfo);

        btnSave = findViewById(R.id.btnEditSave);

        if(userPref.getString("speciality","").equals("null")){
            txtEditSpecialityProviderInfo.setText("");

        }
        else{
            txtEditSpecialityProviderInfo.setText(userPref.getString("speciality",""));
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
        StringRequest request = new StringRequest(Request.Method.POST,Constant.SAVE_USER_DESCRIPTION, res->{

            try {
                JSONObject object = new JSONObject(res);
                if (object.getBoolean("success")){
                    Toast.makeText(this, "Done!", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = userPref.edit();
                    editor.putString("speciality",txtEditSpecialityProviderInfo.getText().toString().trim());
                    editor.putString("description",txtEditDescriptionProviderInfo.getText().toString().trim());
                    editor.apply();
                    finish();
                }
                else if(object.has("error")){
                    Toast.makeText(this, ""+object.getString("error"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            dialog.dismiss();
        },err->{
            Toast.makeText(this, ""+err.getMessage(), Toast.LENGTH_SHORT).show();
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
                map.put("speciality",txtEditSpecialityProviderInfo.getText().toString().trim());
                map.put("description",txtEditDescriptionProviderInfo.getText().toString().trim());
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(EditDescriptionActivity.this);
        queue.add(request);

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

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}