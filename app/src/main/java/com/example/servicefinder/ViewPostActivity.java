package com.example.servicefinder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.servicefinder.Adapters.PostsAdapter;
import com.example.servicefinder.Models.Post;
import com.example.servicefinder.Models.User;
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
    private ImageView providerImg, postImg, userImg;
    private TextView providerName, postDesc, PostDate;
    private SwipeRefreshLayout refreshLayout;
    private SharedPreferences userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);
        userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        providerImg = findViewById(R.id.imgPostProfile);
        postImg = findViewById(R.id.imgPostPhoto);
        userImg = findViewById(R.id.currentUserImg);
        providerName = findViewById(R.id.txtPostName);
        postDesc = findViewById(R.id.txtPostDesc);
        PostDate = findViewById(R.id.txtPostDate);
        refreshLayout = findViewById(R.id.swipePost);
        refreshLayout.setOnRefreshListener(() -> getPost());
        getPost();
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

                    Post post = new Post();
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
}