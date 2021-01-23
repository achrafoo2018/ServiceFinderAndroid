package com.example.servicefinder.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.servicefinder.Constant;
import com.example.servicefinder.EditPostActivity;
import com.example.servicefinder.HomeActivity;
import com.example.servicefinder.Models.Post;
import com.example.servicefinder.R;
import com.example.servicefinder.ViewPostActivity;
import com.example.servicefinder.ViewProfileActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostsHolder> {
    private ArrayList<Post> list;
    private ArrayList<Post> listAll;
    private Context context;
    private SharedPreferences preferences;

    public PostsAdapter(Context context, ArrayList<Post> list) {
        this.context = context;
        this.list = list;
        this.listAll =new ArrayList<>(list);
        preferences = context.getApplicationContext().getSharedPreferences("user",Context.MODE_PRIVATE);

    }

    static class PostsHolder extends RecyclerView.ViewHolder{

        private TextView txtName,txtDate,txtDesc, txtComments;
        private CircleImageView imgProfile;
        private ImageView imgPost;
        private ImageButton btnPostOption;

        public PostsHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtPostName);
            txtDate = itemView.findViewById(R.id.txtPostDate);
            txtComments = itemView.findViewById(R.id.txtPostComments);
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

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onBindViewHolder(@NonNull PostsHolder holder, int position) {
        Post post = list.get(position);
        Picasso.get().load(Constant.URL+post.getUser().getPhoto()).into(holder.imgProfile);
        Picasso.get().load(Constant.URL+post.getPost_picture()).into(holder.imgPost);
        String full_name = post.getUser().getFirst_name()+" "+ post.getUser().getLast_name();
        if(post.getUser().getId()==preferences.getInt("id",0)){
            holder.btnPostOption.setVisibility(View.VISIBLE);
        } else {
            holder.btnPostOption.setVisibility(View.GONE);
        }
        holder.txtName.setText(full_name);
        holder.imgPost.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewPostActivity.class);
            intent.putExtra("postId", String.valueOf(post.getId()));
            context.startActivity(intent);
        });
        holder.txtComments.setOnClickListener(v -> {
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
        holder.txtComments.setText("View all "+post.getComments()+" comments");

        holder.btnPostOption.setOnClickListener(v->{
            PopupMenu popupMenu = new PopupMenu(context,holder.btnPostOption);
            popupMenu.inflate(R.menu.menu_post_options);
            popupMenu.setForceShowIcon(true);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    switch (item.getItemId()){
                        case R.id.item_edit: {
                            Intent i = new Intent(((HomeActivity)context), EditPostActivity.class);
                            i.putExtra("postId",post.getId());
                            i.putExtra("text",post.getDesc());
                            i.putExtra("position",position);
                            i.putExtra("post_picture",post.getPost_picture());
                            i.putExtra("speciality",post.getSpeciality());
                            context.startActivity(i);
                            return true;
                        }
                        case R.id.item_delete: {
                            deletePost(post.getId(),position);
                            return true;
                        }
                    }

                    return false;
                }
            });
            popupMenu.show();
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

    // delete post
    private void deletePost(int postId,int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm");
        builder.setMessage("Delete post?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StringRequest request = new StringRequest(Request.Method.POST,Constant.DELETE_POST, response -> {

                    try {
                        JSONObject object = new JSONObject(response);

                        if (object.getBoolean("success")){
                            list.remove(position);
                            notifyItemRemoved(position);
                            notifyDataSetChanged();
                            listAll.clear();
                            listAll.addAll(list);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                },error -> {

                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        String token = preferences.getString("token","");
                        HashMap<String,String> map = new HashMap<>();
                        map.put("Authorization","Bearer "+token);
                        return map;
                    }

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String,String> map = new HashMap<>();
                        map.put("id",postId+"");
                        return map;
                    }
                };

                RequestQueue queue = Volley.newRequestQueue(context);
                queue.add(request);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });
        builder.show();
    }
    }

