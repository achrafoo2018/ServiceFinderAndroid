package com.example.servicefinder;

import androidx.annotation.NonNull;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditUserInfoActivity extends AppCompatActivity {

    private TextInputLayout layoutName,layoutLastname,layoutEmail;
    private TextInputEditText txtName,txtLastname,txtEmail;
    private TextView txtSelectPhoto;
    private Button btnSave, btnEditDescription;
    private CircleImageView circleImageView;
    private static final int GALLERY_CHANGE_PROFILE = 5;
    private Bitmap bitmap = null;
    private SharedPreferences userPref;
    private ProgressDialog dialog;
    private SlidrInterface slidr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_info);
        slidr = Slidr.attach(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarHome);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        init();
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
    private void init() {
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        layoutLastname = findViewById(R.id.txtEditLayoutLastNameUserInfo);
        layoutName = findViewById(R.id.txtEditLayoutFirstNameUserInfo);
        layoutEmail = findViewById(R.id.txtEditLayoutEmailUserInfo);
        txtName = findViewById(R.id.txtEditFirstNameUserInfo);
        txtLastname = findViewById(R.id.txtEditLastNameUserInfo);
        txtEmail = findViewById(R.id.txtEditEmailUserInfo);
        txtSelectPhoto = findViewById(R.id.txtEditSelectPhoto);
        btnSave = findViewById(R.id.btnEditSave);
        circleImageView = findViewById(R.id.imgEditUserInfo);
        btnEditDescription = findViewById(R.id.btnEditDescription);

        if(userPref.getString("type","").equals("Provider")){
            btnEditDescription.setVisibility(View.VISIBLE);
        }

        Picasso.get().load(getIntent().getStringExtra("imgUrl")).into(circleImageView);
        txtName.setText(userPref.getString("first_name",""));
        txtLastname.setText(userPref.getString("last_name",""));
        txtEmail.setText(userPref.getString("email",""));

        txtSelectPhoto.setOnClickListener(v->{
            Intent i = new Intent(Intent.ACTION_PICK);
            i.setType("image/*");
            startActivityForResult(i,GALLERY_CHANGE_PROFILE);
        });

        btnSave.setOnClickListener(v->{
            if (validate()){
                updateProfile();
            }
        });

    }


    private void updateProfile(){
        dialog.setMessage("Updating");
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST,Constant.SAVE_USER_INFO,res->{

            try {
                JSONObject object = new JSONObject(res);
                if (object.getBoolean("success")){
                    SharedPreferences.Editor editor = userPref.edit();
                    editor.putString("first_name",txtName.getText().toString().trim());
                    editor.putString("last_name",txtLastname.getText().toString().trim());
                    editor.putString("email",txtEmail.getText().toString().trim());
                    editor.apply();
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
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
                map.put("first_name",txtName.getText().toString().trim());
                map.put("last_name",txtLastname.getText().toString().trim());
                map.put("email",txtEmail.getText().toString().trim());
                map.put("profile_picture",bitmapToString(bitmap));
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(EditUserInfoActivity.this);
        queue.add(request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==GALLERY_CHANGE_PROFILE && resultCode==RESULT_OK){
            Uri uri = data.getData();

            circleImageView.setImageURI(uri);

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                bitmap = getResizedBitmap(bitmap,250, 200);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validate(){
        if (txtName.getText().toString().isEmpty()){
            layoutName.setErrorEnabled(true);
            layoutName.setError("First Name is required!");
            return false;
        }
        if (txtLastname.getText().toString().isEmpty()){
            layoutLastname.setErrorEnabled(true);
            layoutLastname.setError("Last Name is required!");
            return false;
        }
        if (txtEmail.getText().toString().isEmpty()){
            layoutEmail.setErrorEnabled(true);
            layoutEmail.setError("Email is required!");
            return false;
        }

        return true;
    }

    private String bitmapToString(Bitmap bitmap) {
        if (bitmap!=null){
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
            byte [] array = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(array,Base64.DEFAULT);
        }

        return "";
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void onChangePasswordClick(View view) {
        Intent intent = new Intent(this, ChangePasswordActivity.class);
        startActivity(intent);
    }


    public void onEditDescriptionClick(View view) {
        Intent intent = new Intent(this, EditDescriptionActivity.class);
        startActivity(intent);
    }
}