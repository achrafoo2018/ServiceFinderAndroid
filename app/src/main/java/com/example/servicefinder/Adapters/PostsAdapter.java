package com.example.servicefinder.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.servicefinder.Constant;
import com.example.servicefinder.Models.Post;
import com.example.servicefinder.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostsHolder> {
    private ArrayList<Post> list;
    private Context context;

    public PostsAdapter(Context context, ArrayList<Post> list) {
        this.context = context;
        this.list = list;

    }

    static class PostsHolder extends RecyclerView.ViewHolder{

        private TextView txtName,txtDate,txtDesc;
        private CircleImageView imgProfile;
        private ImageView imgPost;
        private ImageButton btnPostOption;

        public PostsHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtPostName);
            txtDate = itemView.findViewById(R.id.txtPostDate);
            txtDesc = itemView.findViewById(R.id.txtPostDesc);
            imgProfile = itemView.findViewById(R.id.imgPostProfile);
            imgPost = itemView.findViewById(R.id.imgPostPhoto);
            btnPostOption = itemView.findViewById(R.id.btnPostOption);
            btnPostOption.setVisibility(View.GONE);
        }
    }
    @NonNull
    @Override
    public PostsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_post,parent,false);
        return new PostsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostsHolder holder, int position) {
        Post post = list.get(position);
        Picasso.get().load(Constant.URL+"storage/profile/"+post.getUser().getPhoto()).into(holder.imgProfile);
        Picasso.get().load(Constant.URL+"storage/posts/"+post.getPost_picture()).into(holder.imgPost);
        String full_name = post.getUser().getFirst_name()+" "+ post.getUser().getLast_name();
        holder.txtName.setText(full_name);
        holder.txtDate.setText(post.getDate());
        holder.txtDesc.setText(post.getDesc());
    }

    @Override
    public int getItemCount() {
        return this.list.size();
    }
}
