package com.example.servicefinder.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.servicefinder.Constant;
import com.example.servicefinder.Models.Post;
import com.example.servicefinder.R;
import com.example.servicefinder.ViewPostActivity;
import com.example.servicefinder.ViewProfileActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostsHolder> {
    private ArrayList<Post> list;
    private ArrayList<Post> listAll;
    private Context context;

    public PostsAdapter(Context context, ArrayList<Post> list) {
        this.context = context;
        this.list = list;
        this.listAll =new ArrayList<>(list);
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
        Picasso.get().load(Constant.URL+post.getUser().getPhoto()).into(holder.imgProfile);
        Picasso.get().load(Constant.URL+post.getPost_picture()).into(holder.imgPost);
        String full_name = post.getUser().getFirst_name()+" "+ post.getUser().getLast_name();
        holder.txtName.setText(full_name);
        holder.imgPost.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewPostActivity.class);
            intent.putExtra("postId", String.valueOf(post.getId()));
            context.startActivity(intent);
        });
        holder.txtDate.setText(post.getDate());
        holder.txtDesc.setText(post.getDesc());
        holder.imgProfile.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewProfileActivity.class);
            intent.putExtra("user", post.getUser());
            context.startActivity(intent);
        });
        holder.txtName.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewProfileActivity.class);
            intent.putExtra("user", post.getUser());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return this.list.size();
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            ArrayList<Post> filteredList = new ArrayList<>();
            if (constraint.toString().isEmpty()){
                filteredList.addAll(listAll);
            } else {
                for (Post post : listAll){
                    if(post.getDesc().toLowerCase().contains(constraint.toString().toLowerCase())
                            || post.getUser().getFirst_name().toLowerCase().contains(constraint.toString().toLowerCase())
                            || post.getUser().getLast_name().toLowerCase().contains(constraint.toString().toLowerCase())){
                        filteredList.add(post);
                    }
                }

            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return  results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            list.clear();
            list.addAll((Collection<? extends Post>) results.values);
            notifyDataSetChanged();
        }
    };
    public Filter getFilter() {
        return filter;
    }

    }

