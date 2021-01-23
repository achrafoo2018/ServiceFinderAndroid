package com.example.servicefinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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
    private ImageButton notificationSettings;
    private Toolbar profileToolBar;
    private TextView noNotificationFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_notifications);
        profileToolBar = findViewById(R.id.toolbarViewProfile);
        setSupportActionBar(profileToolBar);
        noNotificationFound = findViewById(R.id.noNotificationFound);
        slidr = Slidr.attach(this);
        preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        recyclerView = findViewById(R.id.recyclerNotifications);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        notificationSettings = findViewById(R.id.notificationsSettings);
        refreshLayout = findViewById(R.id.swipeNotifications);
        getUserNotifications();
        refreshLayout.setOnRefreshListener(() ->{
            getUserNotifications();
        });
        notificationSettings.setOnClickListener(v ->{
            PopupMenu popupMenu = new PopupMenu(getApplicationContext(), notificationSettings);
            popupMenu.inflate(R.menu.menu_all_notifications_options);
            popupMenu.setOnMenuItemClickListener(item -> {

                switch (item.getItemId()){
                    case R.id.item_markAllAsRead: {
                        markAllNotificationsAsRead();
                        return true;
                    }
                    case R.id.item_deleteAll: {
                        deleteAllNotifications();
                        return true;
                    }
                }

                return false;
            });
            popupMenu.show();
        });

    }
    public void markAllNotificationsAsRead(){
        StringRequest request = new StringRequest(Request.Method.GET, Constant.MARK_ALL_NOTIFICATIONS_AS_READ, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")) {

                }else{
                    System.out.println("==========================================");
                    System.out.println(object.getString("request"));
                    System.out.println("==========================================");
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        },error -> {

        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization","Bearer "+ preferences.getString("token",""));
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
        getUserNotifications();
    }
    public void deleteAllNotifications(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage("Delete All Notification?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StringRequest request = new StringRequest(Request.Method.GET, Constant.DELETE_ALL_NOTIFICATIONS, response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (object.getBoolean("success")) {
                        } else {
                            System.out.println("==========================================");
                            System.out.println(object.getString("request"));
                            System.out.println("==========================================");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {

                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("Authorization", "Bearer " + preferences.getString("token", ""));
                        return map;
                    }
                };
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                queue.add(request);
                refreshLayout.post(()->{
                    getUserNotifications();
                });
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {

        });
        builder.show();
    }
    public void getUserNotifications(){
        refreshLayout.setRefreshing(true);
        arrayList = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.GET, Constant.USER_NOTIFICATIONS, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")) {
                 JSONArray notifications = object.getJSONArray("notifications");
                    for(int i=0; i < notifications.length();i++) {
                        JSONObject noti = notifications.getJSONObject(i);
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
                        notf.setRead(!noti.getString("read_at").equals("null"));
                        notf.setId(noti.getString("id"));
                        arrayList.add(notf);
                    }
                    if(arrayList.size() == 0) {
                        recyclerView.setVisibility(View.GONE);
                        noNotificationFound.setVisibility(View.VISIBLE);
                    }
                    adapter = new NotificationsAdapter(this, arrayList);
                    recyclerView.setAdapter(adapter);
                }else{
                    noNotificationFound.setVisibility(View.VISIBLE);
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
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}