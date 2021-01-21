package com.example.servicefinder.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.servicefinder.Constant;
import com.example.servicefinder.Models.Comment;
import com.example.servicefinder.Models.Notification;
import com.example.servicefinder.Models.Post;
import com.example.servicefinder.Models.User;
import com.example.servicefinder.R;

import com.example.servicefinder.ViewProfileActivity;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;


import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationsHolder> {

    private Context context;
    private ArrayList<Notification> arrayList;

    public NotificationsAdapter(Context context, ArrayList<Notification> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
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
        Picasso.get().load(Constant.URL+user.getPhoto()).into(holder.commenterProfilePicture);
        Picasso.get().load(Constant.URL+ post.getPost_picture()).into(holder.postImage);
        }




    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    static class NotificationsHolder extends RecyclerView.ViewHolder{

        private TextView notification,txtCommentDate;
        private CircleImageView commenterProfilePicture;
        private ImageView postImage;
        public NotificationsHolder(@NonNull View itemView) {
            super(itemView);
            notification = itemView.findViewById(R.id.notification);
            txtCommentDate = itemView.findViewById(R.id.notificationDate);
            commenterProfilePicture = itemView.findViewById(R.id.commenterProfilePicture);
            postImage = itemView.findViewById(R.id.notificationPostImage);
        }

    }
}
