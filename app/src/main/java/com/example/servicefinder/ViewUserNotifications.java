package com.example.servicefinder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.servicefinder.Adapters.AccountCommentAdapter;
import com.example.servicefinder.Adapters.NotificationsAdapter;
import com.example.servicefinder.Models.Comment;
import com.example.servicefinder.Models.Notification;
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

public class ViewUserNotifications extends AppCompatActivity {
    private SlidrInterface slidr;
    private SharedPreferences preferences;
    public static ArrayList<Notification> arrayList;
    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private SwipeRefreshLayout refreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_notifications);
        slidr = Slidr.attach(this);
        preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        recyclerView = findViewById(R.id.recyclerNotifications);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        refreshLayout = findViewById(R.id.swipeNotifications);
        getUserNotifications();
        refreshLayout.setOnRefreshListener(() ->{
            getUserNotifications();
        });

    }
    public void getUserNotifications(){
        refreshLayout.setRefreshing(true);
        arrayList = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.GET, Constant.USER_NOTIFICATIONS, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")) {
                 JSONObject notifications = object.getJSONObject("notifications");
                    for(int i = notifications.length() - 1; i >= 0; i--) {
                        JSONObject noti = notifications.getJSONObject(""+i);
                        JSONObject data = noti.getJSONObject("data");
                        JSONObject userObject = data.getJSONObject("user");
                        JSONObject postObject = data.getJSONObject("post");
                        User user = new User();
                        user.setId(userObject.getInt("id"));
                        user.setFirst_name(userObject.getString("first_name"));
                        user.setLast_name(userObject.getString("last_name"));
                        user.setPhoto(userObject.getString("profile_picture"));
                        user.setType(userObject.getString("type"));
                        user.setPhone_number(userObject.getString("phone_number"));
                        user.setEmail(userObject.getString("email"));
                        Post post = new Post();
                        post.setId(postObject.getInt("id"));
                        post.setPost_picture(postObject.getString("post_image"));
                        PrettyTime p = new PrettyTime();
                        String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
                        DateFormat formatter = new SimpleDateFormat(DEFAULT_PATTERN);
                        String created_at = noti.getString("created_at");
                        try{
                            created_at = p.format(formatter.parse(created_at));
                        }catch(ParseException e){
                            e.printStackTrace();
                        }
                        Notification notf = new Notification();
                        notf.setDate(created_at);
                        notf.setPost(post);
                        notf.setUser(user);
                        arrayList.add(notf);
                    }
                    adapter = new NotificationsAdapter(this, arrayList);
                    recyclerView.setAdapter(adapter);
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
            refreshLayout.setRefreshing(false);

        },error -> {
            refreshLayout.setRefreshing(false);

        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                String token = preferences.getString("token", "");
                map.put("Authorization","Bearer "+ token);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }
}