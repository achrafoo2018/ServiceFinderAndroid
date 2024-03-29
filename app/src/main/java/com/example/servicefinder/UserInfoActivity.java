package com.example.servicefinder;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.TextView;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static java.security.AccessController.getContext;

public class UserInfoActivity extends AppCompatActivity {

    private TextInputLayout layoutFirstName, layoutLastName, layoutPhoneNumber;
    private TextInputEditText txtFirstName, txtLastName, txtPhoneNumber;
    private TextView txtSelectPhoto;
    private Button btnContinue;
    private CircleImageView circleImageView;
    private static final int GALLERY_ADD_PROFILE = 1;
    private Bitmap bitmap = null;
    private SharedPreferences userPref;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        init();
    }

    private void init(){
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        layoutFirstName = findViewById(R.id.txtLayoutFirstNameUserInfo);
        layoutLastName = findViewById(R.id.txtLayoutLastNameUserInfo);
        layoutPhoneNumber = findViewById(R.id.txtLayoutPhoneNumberUserInfo);
        txtFirstName = findViewById(R.id.txtFirstNameUserInfo);
        txtLastName = findViewById(R.id.txtLastNameUserInfo);
        txtPhoneNumber = findViewById(R.id.txtPhoneNumberUserInfo);
        txtSelectPhoto = findViewById(R.id.txtSelectPhoto);
        btnContinue = findViewById(R.id.btnContinue);
        circleImageView = findViewById(R.id.imgUserInfo);

        //pick photo from gallery

        txtSelectPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_ADD_PROFILE);
        });


        btnContinue.setOnClickListener(v -> {
            // validate fields

            if(validate()){
                saveUserInfo();
            }
        });

    }
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, true);
        bm.recycle();
        return resizedBitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_ADD_PROFILE && resultCode == RESULT_OK){
            Uri imgUri = data.getData();
            circleImageView.setImageURI(imgUri);

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
                bitmap = getResizedBitmap(bitmap,300, 260);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validate(){

        if (txtFirstName.getText().toString().isEmpty()){
            layoutFirstName.setErrorEnabled(true);
            layoutFirstName.setError("First Name is required");
            return false;
        }
        if (txtLastName.getText().toString().isEmpty()){
            layoutLastName.setErrorEnabled(true);
            layoutLastName.setError("Last Name is required");
            return false;
        }
        if (txtPhoneNumber.getText().toString().isEmpty()){
            layoutPhoneNumber.setErrorEnabled(true);
            layoutPhoneNumber.setError("Phone Number is required");
            return false;
        }

        return true;
    }

    private void saveUserInfo(){
        dialog.setMessage("Saving");
        dialog.show();
        String firstName = txtFirstName.getText().toString().trim();
        String lastName = txtLastName.getText().toString().trim();
        String phone_number = txtPhoneNumber.getText().toString().trim();

        StringRequest request = new StringRequest(Request.Method.POST,Constant.SAVE_USER_INFO, response -> {

            try {
                JSONObject object = new JSONObject(response);
                    if (object.getBoolean("success")){
                        SharedPreferences.Editor editor = userPref.edit();
                        JSONObject user = object.getJSONObject("user");
                        editor.putString("first_name",user.getString("first_name"));
                        editor.putString("last_name",user.getString("last_name"));
                        editor.putString("phone_number",user.getString("phone_number"));
                        editor.putString("profile_picture", user.getString("profile_picture"));
                        editor.apply();
                        Intent intent;
                        if(user.getString("type").toLowerCase().equals("provider")){
                            intent = new Intent(UserInfoActivity.this, ProviderInfo.class);
                        }else{
                            intent = new Intent(UserInfoActivity.this, HomeActivity.class);
                        }
                        startActivity(intent);
                        //Toast.makeText(getApplicationContext(), "Register Success", Toast.LENGTH_SHORT).show();
                        finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            dialog.dismiss();

        },error -> {
            error.printStackTrace();
            dialog.dismiss();
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = userPref.getString("token", "");
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization", "Bearer "+token);
                return map;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("id", String.valueOf(userPref.getInt("id", -1)));
                map.put("first_name", firstName);
                map.put("last_name", lastName);
                map.put("phone_number", phone_number);
                map.put("profile_picture", bitmapToString(bitmap));
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(UserInfoActivity.this);
        queue.add(request);
    }

    private String bitmapToString(Bitmap bitmap) {

        if (bitmap!=null){
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte [] array = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(array, Base64.DEFAULT);
        }

        return "";
    }


}