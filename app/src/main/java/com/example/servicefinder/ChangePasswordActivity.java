package com.example.servicefinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

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
import java.util.Objects;

public class ChangePasswordActivity extends AppCompatActivity {
    private TextInputLayout passwordLayout, newPasswordLayout, confirmPasswordLayout;
    private TextInputEditText txtPassword, txtNewPassword, txtConfirmPassword;
    private Button btnSave;
    private SharedPreferences userPref;
    private ProgressDialog dialog;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarHome);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        init();


    }


    private void init(){
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        passwordLayout = findViewById(R.id.passwordLayout);
        newPasswordLayout = findViewById(R.id.newPasswordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        txtPassword = findViewById(R.id.txtPassword);
        txtNewPassword = findViewById(R.id.txtNewPassword);
        txtConfirmPassword = findViewById(R.id.txtConfirmPassword);
        btnSave = findViewById(R.id.btnEditSave);
        userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);

        btnSave.setOnClickListener(v->{
            if (validate()){
                changePassword();
            }
        });

    }

    private void changePassword() {

        dialog.setMessage("Changing Password");
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST,Constant.CHANGE_PASSWORD, res->{

            try {
                JSONObject object = new JSONObject(res);
                if (object.getBoolean("success")){
                    Toast.makeText(this, object.getString("message"), Toast.LENGTH_LONG).show();
                    finish();
                }
                else{
                    if (object.getInt("code") == 502){
                        dialog.dismiss();
                        passwordLayout.setErrorEnabled(true);
                        passwordLayout.setError("Password incorrect!");
                    }
                    else{
                        dialog.dismiss();
                        Toast.makeText(this, object.getString("error"), Toast.LENGTH_LONG).show();

                    }
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
                map.put("password",txtNewPassword.getText().toString());
                map.put("current_password",txtPassword.getText().toString());
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(ChangePasswordActivity.this);
        queue.add(request);

    }

    private boolean validate() {

        if (txtPassword.getText().toString().isEmpty()){
            passwordLayout.setErrorEnabled(true);
            passwordLayout.setError("Password is required!");
            return false;
        }
        if (txtNewPassword.getText().toString().isEmpty()){
            newPasswordLayout.setErrorEnabled(true);
            newPasswordLayout.setError("New Password is required!");
            return false;
        }
        if (txtConfirmPassword.getText().toString().isEmpty()){
            confirmPasswordLayout.setErrorEnabled(true);
            confirmPasswordLayout.setError("Confirm Password is required!");
            return false;
        }

        if (!txtNewPassword.getText().toString().equals(txtConfirmPassword.getText().toString())){
            newPasswordLayout.setErrorEnabled(true);
            newPasswordLayout.setError("Confirm Password incorrect!");
            return false;
        }


        return true;

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}