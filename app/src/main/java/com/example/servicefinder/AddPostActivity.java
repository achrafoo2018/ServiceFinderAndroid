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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.servicefinder.Fragments.HomeFragment;
import com.example.servicefinder.Models.Post;
import com.example.servicefinder.Models.User;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {
    private Button btnPost;
    private ImageView imgPost;
    private EditText txtDesc;
    private Bitmap bitmap = null;
    private static final int GALLERY_CHANGE_POST = 3;
    private ProgressDialog dialog;
    private SharedPreferences preferences;
    private SlidrInterface slidr;
    private SearchableSpinner spinner;
    private ArrayList<String> specialities = new ArrayList<>();

    public AddPostActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        spinner = findViewById(R.id.spinner);
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
        preferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        btnPost = findViewById(R.id.btnAddPost);
        imgPost = findViewById(R.id.imgAddPost);
        txtDesc = findViewById(R.id.txtDescAddPost);

        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        btnPost.setOnClickListener(v->{
            if(!txtDesc.getText().toString().isEmpty()){
                if(bitmap != null )
                    post();
                else
                    Toast.makeText(this, "Image is required", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Post description is required", Toast.LENGTH_SHORT).show();
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
                        specialities.add(speciality.getString("speciality"));
                    }
                    ArrayAdapter<String> adapter=new ArrayAdapter<>(AddPostActivity.this, android.R.layout.simple_spinner_item, specialities);
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

    private void post(){

        dialog.setMessage("Posting");
        dialog.show();

        StringRequest request = new StringRequest(Request.Method.POST,Constant.ADD_POST, response -> {

            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")){
                    JSONObject postObject = object.getJSONObject("post");
                    JSONObject userObject = postObject.getJSONObject("user");

                    //User
                    User user = new User();
                    user.setId(userObject.getInt("id"));
                    user.setFirst_name(userObject.getString("first_name"));
                    user.setLast_name(userObject.getString("last_name"));
                    user.setPhoto(userObject.getString("profile_picture"));

                    //Post
                    Post post = new Post();
                    post.setUser(user);
                    post.setId(postObject.getInt("id"));
                    post.setPost_picture(postObject.getString("post_image"));
                    post.setDesc(postObject.getString("desc"));
                    post.setSpeciality(postObject.getString("speciality"));
                    PrettyTime p = new PrettyTime();
                    String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
                    DateFormat formatter = new SimpleDateFormat(DEFAULT_PATTERN);
                    String created_at = postObject.getString("created_at");
                    try{
                        created_at = p.format(formatter.parse(postObject.getString("created_at")));
                    }catch(ParseException e){
                        e.printStackTrace();
                    }
                    post.setDate(created_at);
                    post.setComments(0);
                    HomeFragment.arrayList.add(0, post);
                    HomeFragment.recyclerView.getAdapter().notifyItemInserted(0);
                    HomeFragment.recyclerView.getAdapter().notifyDataSetChanged();
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


            // add token to header
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = preferences.getString("token","");
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization","Bearer "+token);
                return map;
            }

            // add params
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("id", String.valueOf(preferences.getInt("id", -1)));
                map.put("desc",txtDesc.getText().toString().trim());
                map.put("photo",bitmapToString(bitmap));
                map.put("speciality", spinner.getSelectedItem().toString());
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(AddPostActivity.this);
        queue.add(request);

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



    public void cancelPost(View view) {
        super.onBackPressed();
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