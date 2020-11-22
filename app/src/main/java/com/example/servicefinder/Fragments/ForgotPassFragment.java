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

import com.example.servicefinder.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPassFragment extends Fragment {
    private View view;
    private TextInputLayout layoutEmail;
    private TextInputEditText txtEmail;
    private TextView goToLogin;
    private Button resetPass;

    public ForgotPassFragment(){}
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_forgot_pass,container,false);
        init();
        return view;
    }

    private void init() {

        layoutEmail = view.findViewById(R.id.txtLayoutEmailForgotPass);
        goToLogin = view.findViewById(R.id.gotologin);
        txtEmail = view.findViewById(R.id.txtEmailForgotPass);
        resetPass = view.findViewById(R.id.resetPass);

        goToLogin.setOnClickListener(v ->{
            //change fragments
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameAuthContainer, new SignInFragment()).commit();
        });
        resetPass.setOnClickListener(v -> {
                    //validate fields first
                    if (validate()) {
                        //sendMail();
                    }
        });


            txtEmail.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!txtEmail.getText().toString().isEmpty()) {
                        layoutEmail.setErrorEnabled(false);
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
        return true;
    }

}
