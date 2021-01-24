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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.servicefinder.Fragments.HomeFragment;
import com.example.servicefinder.Models.Post;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
import com.squareup.picasso.Picasso;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditPostActivity extends AppCompatActivity {

    private int id= 0;
    private EditText txtDesc;
    private Button btnSave;
    private Bitmap bitmap = null;
    private ProgressDialog dialog;
    private ImageView imgPost;
    private SharedPreferences sharedPreferences;
    private static final int GALLERY_CHANGE_POST = 3;
    private SearchableSpinner spinner;
    private ArrayList<String> specialities = new ArrayList<>();
    private Bundle extras;
    private SlidrInterface slidr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);
        slidr = Slidr.attach(this);
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
        sharedPreferences = getApplication().getSharedPreferences("user", Context.MODE_PRIVATE);
        txtDesc = findViewById(R.id.txtDescEditPost);
        btnSave = findViewById(R.id.btnEditPost);
        imgPost = findViewById(R.id.imgEditPost);
        spinner = findViewById(R.id.spinner);
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        id = getIntent().getIntExtra("postId",0);
        txtDesc.setText(getIntent().getStringExtra("text"));
        extras = getIntent().getExtras();
        Picasso.get().load(Constant.URL+extras.getString("post_picture")).into(imgPost);
        specialities.add(extras.getString("speciality"));

        btnSave.setOnClickListener(v->{
            if (!txtDesc.getText().toString().isEmpty()){
                savePost();
            }
        });
        getSpecialities();
    }

    private void getSpecialities(){
        StringRequest request = new StringRequest(Request.Method.GET,Constant.SPECIALITIES, response ->{
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")) {
                    JSONArray array = new JSONArray(object.getString("specialities"));
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject speciality = array.getJSONObject(i);
                        if(speciality.getString("speciality").equals(extras.getString("speciality"))){
                            continue;
                        }
                        specialities.add(speciality.getString("speciality"));

                    }
                    ArrayAdapter<String> adapter=new ArrayAdapter<>(EditPostActivity.this, android.R.layout.simple_spinner_item, specialities);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);

                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }, Throwable::printStackTrace);
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }

    private void savePost() {
        dialog.setMessage("Saving");
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST,Constant.UPDATE_POST,response -> {

            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")){
                    // update the post in recycler view
                    Intent intent = new Intent(getApplicationContext(), ViewPostActivity.class);
                    intent.putExtra("postId", String.valueOf(id));
                    startActivity(intent);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        },error -> {
            error.printStackTrace();
        }){

            //add token to header


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = sharedPreferences.getString("token","");
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization","Bearer "+token);
                return map;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("id",id+"");
                map.put("desc",txtDesc.getText().toString());
                map.put("image",bitmapToString(bitmap));
                map.put("speciality", spinner.getSelectedItem().toString());

                return map;
            }
        };
        dialog.dismiss();
        RequestQueue queue = Volley.newRequestQueue(EditPostActivity.this);
        queue.add(request);
    }

    public void cancelEdit(View view){
        super.onBackPressed();
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

    public void changePhoto(View view) {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i, GALLERY_CHANGE_POST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CHANGE_POST && resultCode == RESULT_OK) {
            Uri imgUri = data.getData();
            imgPost.setImageURI(imgUri);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 640);
            imgPost.setLayoutParams(layoutParams);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
                bitmap = getResizedBitmap(bitmap,760, 560);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

