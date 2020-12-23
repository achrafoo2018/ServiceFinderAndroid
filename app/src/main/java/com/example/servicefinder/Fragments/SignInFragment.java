package com.example.servicefinder.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.servicefinder.Constant;
import com.example.servicefinder.HomeActivity;
import com.example.servicefinder.ProfileActivity;
import com.example.servicefinder.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignInFragment extends Fragment {
    private View view;
    private TextInputLayout layoutEmail,layoutPassword;
    private TextInputEditText txtEmail,txtPassword;
    private TextView goToRegister,forgotPassword;
    private Button btnLogin;
    private ProgressDialog dialog;


    public SignInFragment(){}
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_sign_in,container,false);
        init();
        return view;
    }

    private void init() {
        layoutPassword = view.findViewById(R.id.txtLayoutPasswordSignIn);
        layoutEmail = view.findViewById(R.id.txtLayoutEmailSignIn);
        txtPassword = view.findViewById(R.id.txtPasswordSignIn);
        goToRegister = view.findViewById(R.id.goToRegister);
        txtEmail = view.findViewById(R.id.txtEmailSignIn);
        btnLogin = view.findViewById(R.id.btnLogin);
        forgotPassword =view.findViewById(R.id.forgotPassword);
        dialog = new ProgressDialog(getContext());
        dialog.setCancelable(false);


        forgotPassword.setOnClickListener(v ->{
            //change fragments
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameAuthContainer, new ForgotPassFragment()).commit();
        });

        goToRegister.setOnClickListener(v ->{
            //change fragments
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameAuthContainer, new SignUpFragment()).commit();
        });

        btnLogin.setOnClickListener(v -> {
            //validate fields first
            if (validate()) {
                login();
            }
        });

     txtEmail.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!txtEmail.getText().toString().isEmpty()){
                layoutEmail.setErrorEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    });

        txtPassword.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (txtPassword.getText().toString().length()>7){
                layoutPassword.setErrorEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    });
}


    private boolean validate() {
            if (txtEmail.getText().toString().isEmpty()){
                layoutEmail.setErrorEnabled(true);
                layoutEmail.setError("Email is Required");
                return false;
            }
            if (txtPassword.getText().toString().length()<7){
                layoutPassword.setErrorEnabled(true);
                layoutPassword.setError("Required at least 8 characters");
                return false;
            }
            return true;
        }

    private void login() {
        dialog.setMessage("Logging in");
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, Constant.LOGIN,response -> {
            //we get response if connection success
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")){
                    JSONObject user = object.getJSONObject("user");
                    //make shared preference user
                    SharedPreferences userPref = getActivity().getApplicationContext().getSharedPreferences("user",getContext().MODE_PRIVATE);
                    SharedPreferences.Editor editor = userPref.edit();
                    editor.putString("token",object.getString("token"));
                    editor.putString("first_name",user.getString("first_name"));
                    editor.putInt("id",user.getInt("id"));
                    editor.putString("last_name",user.getString("last_name"));
                    editor.putString("type",user.getString("type"));
                    editor.apply();
                    Intent intent = new Intent(getActivity().getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dialog.dismiss();
        },error -> {
            // error if connection not success
            error.printStackTrace();
            dialog.dismiss();

        }){

            // add parameters


            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("email",txtEmail.getText().toString().trim());
                map.put("password",txtPassword.getText().toString());
                return map;
            }
        };

        //add this request to requestqueue
        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(request);
    }

}

