package com.example.servicefinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.servicefinder.Adapters.AccountCommentAdapter;
import com.example.servicefinder.Adapters.PostsAdapter;
import com.example.servicefinder.Models.Comment;
import com.example.servicefinder.Models.Post;
import com.example.servicefinder.Models.User;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
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

public class ViewPostActivity extends AppCompatActivity {
    private ImageView providerImg, postImg, userImg, btnComment;
    private TextView providerName, postDesc, PostDate;
    private SwipeRefreshLayout refreshLayout;
    private SharedPreferences userPref;
    private EditText txtComment;
    private AccountCommentAdapter adapter;
    private ProgressDialog dialog;
    private SlidrInterface slidr;
    private ArrayList<Comment> arrayList;
    private RecyclerView recyclerView;

    private Post post = new Post();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);
        slidr = Slidr.attach(this);
        userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        providerImg = findViewById(R.id.imgPostProfile);
        postImg = findViewById(R.id.imgPostPhoto);
        userImg = findViewById(R.id.currentUserImg);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarHome);
        setSupportActionBar(toolbar);
        providerName = findViewById(R.id.txtPostName);
        postDesc = findViewById(R.id.txtPostDesc);
        PostDate = findViewById(R.id.txtPostDate);
        btnComment = findViewById(R.id.btnComment);
        recyclerView = findViewById(R.id.recyclerPost);
        txtComment = findViewById(R.id.txtComment);
        refreshLayout = findViewById(R.id.swipePost);
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        refreshLayout.setOnRefreshListener(() ->{
                getPost();
                getData();
        });
        getPost();
        getData();

        providerName.setOnClickListener(v->{

            Intent intent = new Intent(this, ViewProfileActivity.class);
            intent.putExtra("user", post.getUser());
            startActivity(intent);

        });
        providerImg.setOnClickListener(v->{

            Intent intent = new Intent(this, ViewProfileActivity.class);
            intent.putExtra("user", post.getUser());
            startActivity(intent);

        });

        btnComment.setOnClickListener(v -> {
            dialog.setMessage("Commenting");
            dialog.show();
            StringRequest request = new StringRequest(Request.Method.POST, Constant.CREATE_POST_COMMENT, response -> {
            },error -> {
                dialog.dismiss();
            }){
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String,String> map = new HashMap<>();
                    map.put("user_id", String.valueOf(userPref.getInt("id",0)));
                    map.put("post_id", String.valueOf(post.getId()));
                    map.put("comment", txtComment.getText().toString().trim());
                    return map;
                }

            };
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            queue.add(request);
            txtComment.setText("");
            dialog.dismiss();
            refreshLayout.setRefreshing(true);
            getData();
            refreshLayout.setRefreshing(false);

            // closing keyboard after clicking on comment
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(refreshLayout.getWindowToken(), 0);
        });

        //Commenting on post
    }

    private void getPost() {
        refreshLayout.setRefreshing(true);

        StringRequest request = new StringRequest(Request.Method.GET, Constant.GET_POST, response -> {

            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")) {
                    JSONObject postObject = object.getJSONObject("post");
                    JSONObject userObject = postObject.getJSONObject("user");
                    User user = new User();
                    user.setId(userObject.getInt("id"));
                    user.setFirst_name(userObject.getString("first_name"));
                    user.setLast_name(userObject.getString("last_name"));
                    user.setPhoto(userObject.getString("profile_picture"));
                    user.setEmail(userObject.getString("email"));
                    user.setPhone_number(userObject.getString("phone_number"));
                    user.setType(userObject.getString("type"));

                    post.setId(postObject.getInt("id"));
                    post.setUser(user);
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
                    post.setDesc(postObject.getString("desc"));
                    post.setPost_picture(postObject.getString("post_image"));
                    Picasso.get().load(Constant.URL+post.getUser().getPhoto()).into(providerImg);
                    Picasso.get().load(Constant.URL+post.getPost_picture()).into(postImg);
                    Picasso.get().load(Constant.URL+userPref.getString("profile_picture", "")).into(userImg);
                    String full_name = post.getUser().getFirst_name() + " " + post.getUser().getLast_name();
                    providerName.setText(full_name);
                    postDesc.setText(post.getDesc());
                    PostDate.setText(post.getDate());
                }
                } catch (JSONException e) {
                e.printStackTrace();
            }
            refreshLayout.setRefreshing(false);

        }, error -> {
            error.printStackTrace();
            refreshLayout.setRefreshing(false);

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization","Bearer "+ getIntent().getExtras().getString("postId"));
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }
    private void getData() {
        refreshLayout.setRefreshing(true);
        arrayList = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.POST, Constant.COMMENTS_ON_POST, response -> {

            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")){
                    JSONArray comments = object.getJSONArray("comments");
                    if (comments.length() != 0)
                        recyclerView.setVisibility(View.VISIBLE);
                    for (int i = 0; i < comments.length(); i++){
                        JSONObject c = comments.getJSONObject(i);
                        JSONObject uObject = c.getJSONObject("user");
                        
                        User u = new User();
                        u.setId(uObject.getInt("id"));
                        u.setFirst_name(uObject.getString("first_name"));
                        u.setLast_name(uObject.getString("last_name"));
                        u.setPhoto(uObject.getString("profile_picture"));
                        u.setEmail(uObject.getString("email"));
                        u.setPhone_number(uObject.getString("phone_number"));
                        u.setType(uObject.getString("type"));

                        Comment comment = new Comment();
                        comment.setUser(u);
                        comment.setId(c.getInt("id"));
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
                    adapter = new AccountCommentAdapter(this,arrayList, R.layout.layout_post_comment);
                    recyclerView.setAdapter(adapter);

                }

            } catch (JSONException e) {
            }

            refreshLayout.setRefreshing(false);


        }, error -> {
            refreshLayout.setRefreshing(false);


        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization","Bearer "+ getIntent().getExtras().getString("postId"));
                return map;
            }

        };

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}