package com.example.servicefinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.servicefinder.Adapters.AccountCommentAdapter;
import com.example.servicefinder.Models.Comment;
import com.example.servicefinder.Models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends AppCompatActivity {

    private View view;
    private MaterialToolbar toolbar;
    private CircleImageView imgProfile;
    private TextView txtName, txtPostsCount,service,speciality,phone_number,description;
    private Button btnEditAccount;
    private ImageView btnComment;
    private SwipeRefreshLayout refreshLayout,swipeProfile2;
    private RecyclerView recyclerView;
    private ArrayList<Comment> arrayList;
    private SharedPreferences preferences;
    private AccountCommentAdapter adapter;
    private String imgUrl = "";
    private EditText txtComment;
    private User commenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        init();
    }

    private void init() {
        preferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        toolbar = findViewById(R.id.toolbarAccount);
//        ViewProfileActivity.this.setSupportActionBar(toolbar);
//        setHasOptionsMenu(true);
        imgProfile = findViewById(R.id.imgAccountProfile);
        txtName = findViewById(R.id.txtAccountName);
        txtPostsCount = findViewById(R.id.txtAccountPostCount);
        recyclerView = findViewById(R.id.recyclerAccount);
        swipeProfile2 = findViewById(R.id.swipeProfile2);
        service = findViewById(R.id.service);
        speciality = findViewById(R.id.speciality);
        phone_number = findViewById(R.id.phone_number);
        description = findViewById(R.id.description);
        btnComment = findViewById(R.id.btnComment);
        txtComment = findViewById(R.id.txtComment);
        commenter = (User) getIntent().getSerializableExtra("user");


        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ViewProfileActivity.this));

        getData();

        swipeProfile2.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });


        //Commenting on profile

        btnComment.setOnClickListener(v -> {

            StringRequest request = new StringRequest(Request.Method.POST, Constant.CREATE_COMMENT, response -> {

                try {

                    JSONObject object = new JSONObject(response);
                    if(object.getBoolean("success")){

                    }
                    else if (object.has("error")){
                        Toast.makeText(ViewProfileActivity.this, object.getString("error"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(ViewProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                swipeProfile2.setRefreshing(true);
            },error -> {
                Toast.makeText(ViewProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

            }){

                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String,String> map = new HashMap<>();
                    map.put("user_id", String.valueOf(preferences.getInt("id",0)));
                    map.put("provider_id", String.valueOf(commenter.getId()));
                    map.put("comment", txtComment.getText().toString().trim());
                    return map;
                }

            };

            RequestQueue queue = Volley.newRequestQueue(ViewProfileActivity.this);
            queue.add(request);
            txtComment.setText("");
            swipeProfile2.post(() -> {
                swipeProfile2.setRefreshing(true);
                getData();
                swipeProfile2.setRefreshing(false);
            });

        });

        //Commenting on profile

    }

    private void getData() {
        arrayList = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.GET, Constant.COMMENTS_ON_MY_PROFILE, response -> {

            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")){
                    JSONArray comments = object.getJSONArray("comments");
                    JSONObject userObject = object.getJSONObject("user");

                    User user = new User();
                    user.setId(userObject.getInt("id"));
                    user.setFirst_name(userObject.getString("first_name"));
                    user.setLast_name(userObject.getString("last_name"));
                    user.setPhoto(userObject.getString("profile_picture"));

                    txtName.setText(user.getFirst_name()+" "+user.getLast_name());
                    //                    Rating here idk

                    if(preferences.getString("service","").equals("null")){
                        service.setText("");
                    }
                    else{
                        service.setText(" " + preferences.getString("service",""));
                    }
                    if(preferences.getString("speciality","").equals("null")){
                        speciality.setText("");

                    }
                    else{
                        speciality.setText(" " + preferences.getString("speciality",""));
                    }
                    if(preferences.getString("phone_number","").equals("null")){
                        phone_number.setText("");
                    }
                    else{
                        phone_number.setText(" " + preferences.getString("phone_number",""));
                    }
                    if(preferences.getString("description","").equals("null")){
                        description.setText("");
                    }
                    else{
                        description.setText(" " + preferences.getString("description",""));

                    }

                    if (comments.length() != 0)
                        recyclerView.setVisibility(View.VISIBLE);
                    for (int i = 0; i < comments.length(); i++){
                        JSONObject c = comments.getJSONObject(i);
                        Comment comment = new Comment();
                        comment.setComment(c.getString("comment"));
                        comment.setCommenterName(c.getJSONObject("user").getString("first_name")+" "+c.getJSONObject("user").getString("last_name"));
                        PrettyTime p = new PrettyTime();
                        String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
                        DateFormat formatter = new SimpleDateFormat(DEFAULT_PATTERN);
                        String created_at = c.getString("created_at");
                        try{
                            created_at = p.format(formatter.parse(c.getString("created_at")));
                        }catch(ParseException e){
                            e.printStackTrace();
                        }
                        comment.setCommentDate(created_at);
                        comment.setCommenterProfilePicture(c.getJSONObject("user").getString("profile_picture"));
                        arrayList.add(comment);

                    }
                    Picasso.get().load(Constant.URL+userObject.getString("profile_picture")).into(imgProfile);
                    adapter = new AccountCommentAdapter(ViewProfileActivity.this,arrayList);
                    recyclerView.setAdapter(adapter);
                    imgUrl = Constant.URL+userObject.getString("profile_picture");


                }
                else if (object.has("error")){
                    Toast.makeText(ViewProfileActivity.this, object.getString("error"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(ViewProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            swipeProfile2.setRefreshing(false);


        }, error -> {
            Toast.makeText(ViewProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            swipeProfile2.setRefreshing(false);


        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String email = commenter.getEmail();
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization","Bearer "+email);
                return map;
            }

        };

        RequestQueue queue = Volley.newRequestQueue(ViewProfileActivity.this);
        queue.add(request);
    }
}