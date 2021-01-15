package com.example.servicefinder.Fragments;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.example.servicefinder.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgotPassFragment extends Fragment {
    private View view;
    private TextInputLayout layoutEmail;
    private TextInputEditText txtEmail;
    private TextView goToLogin, msgTxt;
    private Button resetPass;
    private ProgressDialog dialog;

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
        dialog = new ProgressDialog(getContext());
        dialog.setCancelable(false);

        msgTxt = view.findViewById(R.id.txtMsg);
        goToLogin.setOnClickListener(v ->{
            //change fragments
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameAuthContainer, new SignInFragment()).commit();
        });
        resetPass.setOnClickListener(v -> {
                    //validate fields first
                    if (validate()) {
                        sendMail();
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


    private void sendMail() {
        dialog.setMessage("Sending email");
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, Constant.SENDPASSWORDRESETLINK, response -> {
            //we get response if connection success
            try {
                JSONObject object = new JSONObject(response);
                msgTxt.setText(object.getString("message"));
                msgTxt.setTextColor(Color.parseColor("#02c73d"));
                msgTxt.setVisibility(View.VISIBLE);
                txtEmail.setText("");

            } catch (JSONException ignored) {
                msgTxt.setText("We have e-mailed your password reset link!");
                msgTxt.setTextColor(Color.parseColor("#02c73d"));
                msgTxt.setVisibility(View.VISIBLE);
                txtEmail.setText("");
            }
            dialog.dismiss();

        }, error -> {
            error.printStackTrace();
            msgTxt.setText("We have e-mailed your password reset link!");
            msgTxt.setTextColor(Color.parseColor("#02c73d"));
            msgTxt.setVisibility(View.VISIBLE);
            txtEmail.setText("");
            dialog.dismiss();
        }) {

            // add parameters


            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("email", txtEmail.getText().toString().trim());
                return map;
            }
        };

        //add this request to requestqueue
        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(request);
    }
}




