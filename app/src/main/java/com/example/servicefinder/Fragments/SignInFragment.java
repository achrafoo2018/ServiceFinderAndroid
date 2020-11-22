package com.example.servicefinder.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.example.servicefinder.Constant;
import com.example.servicefinder.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignInFragment extends Fragment {
    private View view;
    private TextInputLayout layoutEmail,layoutPassword;
    private TextInputEditText txtEmail,txtPassword;
    private TextView goToRegister,forgotPassword;
    private Button btnLogin;


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
            if (txtPassword.getText().toString().length()<8){
                layoutPassword.setErrorEnabled(true);
                layoutPassword.setError("Required at least 8 characters");
                return false;
            }
            return true;
        }

    private void login() {
        StringRequest request = new StringRequest(Request.Method.POST, Constant.LOGIN, response -> {
            //we get response if connection success
        },error -> {
            // error if connection not success
            error.printStackTrace();
    });

    }
}

