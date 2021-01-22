package com.example.servicefinder.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.servicefinder.Constant;
import com.example.servicefinder.Models.Notification;
import com.example.servicefinder.Models.Post;
import com.example.servicefinder.Models.User;
import com.example.servicefinder.R;

import com.example.servicefinder.ViewPostActivity;
import com.squareup.picasso.Picasso;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationsHolder> {

    private Context context;
    private ArrayList<Notification> arrayList;
    private SharedPreferences userPref;

    public NotificationsAdapter(Context context, ArrayList<Notification> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        userPref = context.getSharedPreferences("user", Context.MODE_PRIVATE);

    }

    @NonNull
    @Override
    public NotificationsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_notification_item, parent, false);
        return new NotificationsHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsHolder holder, int position) {
        Notification notif = arrayList.get(position);
        User user = notif.getUser();
        Post post = notif.getPost();
        String text = user.getFirst_name() + " " + user.getLast_name() + " commented on your post.";
        holder.notification.setText(text);
        holder.txtCommentDate.setText(notif.getDate());
        if(!notif.isRead()){
            holder.notifLayout.setBackgroundColor(context.getResources().getColor(R.color.colorNotification));
        }else{
            holder.notifLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
        Picasso.get().load(Constant.URL+user.getPhoto()).into(holder.commenterProfilePicture);
        Picasso.get().load(Constant.URL+ post.getPost_picture()).into(holder.postImage);
        holder.notifLayout.setOnClickListener(v -> {
            markNotificationAsRead(notif, holder);
            Intent intent = new Intent(context, ViewPostActivity.class);
            intent.putExtra("postId", String.valueOf(post.getId()));
            context.startActivity(intent);
        });
        holder.btnPostOption.setOnClickListener(v->{

            PopupMenu popupMenu = new PopupMenu(context,holder.btnPostOption);
            popupMenu.inflate(R.menu.menu_notification_options);
            popupMenu.setOnMenuItemClickListener(item -> {

                switch (item.getItemId()){
                    case R.id.item_markAsRead: {
                        markNotificationAsRead(notif, holder);
                        return true;
                    }
                    case R.id.item_delete: {
                        deleteNotification(notif);
                        return true;
                    }
                }

                return false;
            });
            popupMenu.show();
        });
        }


    private void markNotificationAsRead(Notification notification, NotificationsHolder holder){
        StringRequest request = new StringRequest(Request.Method.GET, Constant.MARK_NOTIFICATION_AS_READ, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")) {
                    holder.notifLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
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
                map.put("Authorization","Bearer "+ userPref.getString("token","") + "+" + notification.getId());
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this.context);
        queue.add(request);
    }
    private void deleteNotification(Notification notification){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm");
        builder.setMessage("Delete Notification?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StringRequest request = new StringRequest(Request.Method.GET, Constant.DELETE_NOTIFICATION, response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (object.getBoolean("success")) {
                            Toast.makeText(context, "Notification deleted successfully !", Toast.LENGTH_SHORT).show();
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
                        map.put("Authorization", "Bearer " + userPref.getString("token", "") + "+" + notification.getId());
                        return map;
                    }
                };
                RequestQueue queue = Volley.newRequestQueue(context);
                queue.add(request);
            }
    });
        builder.setNegativeButton("Cancel", (dialog, which) -> {

        });
        builder.show();
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    static class NotificationsHolder extends RecyclerView.ViewHolder{

        private TextView notification,txtCommentDate;
        private CircleImageView commenterProfilePicture;
        private ImageView postImage;
        private LinearLayout notifLayout;
        private ImageButton btnPostOption;

        public NotificationsHolder(@NonNull View itemView) {
            super(itemView);
            notification = itemView.findViewById(R.id.notification);
            txtCommentDate = itemView.findViewById(R.id.notificationDate);
            commenterProfilePicture = itemView.findViewById(R.id.commenterProfilePicture);
            postImage = itemView.findViewById(R.id.notificationPostImage);
            notifLayout = itemView.findViewById(R.id.notifLayout);
            btnPostOption = itemView.findViewById(R.id.btnPostOption);

        }

    }
}
