package com.example.servicefinder.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.servicefinder.Constant;
import com.example.servicefinder.Models.Comment;
import com.example.servicefinder.R;

import com.example.servicefinder.ViewPostActivity;
import com.example.servicefinder.ViewProfileActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountCommentAdapter extends RecyclerView.Adapter<AccountCommentAdapter.AccountCommentHolder> {

    private Context context;
    private ArrayList<Comment> arrayList;
    private int layout;

    public AccountCommentAdapter(Context context, ArrayList<Comment> arrayList, int layout) {
        this.context = context;
        this.arrayList = arrayList;
        this.layout = layout;
    }

    @NonNull
    @Override
    public AccountCommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new AccountCommentHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountCommentHolder holder, int position) {
        Comment comment = arrayList.get(position);
        holder.commentorName.setText(comment.getCommenterName());
        holder.comment.setText(comment.getComment());
        holder.txtCommentDate.setText(comment.getCommentDate());
        if(this.layout == R.layout.layout_account_comment)
            holder.ratingBar.setRating(comment.getRating());
        Picasso.get().load(Constant.URL+comment.getCommenterProfilePicture()).into(holder.commenterProfilePicture);
        SharedPreferences preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        String name = preferences.getString("first_name",null) + " " + preferences.getString("last_name",null);
        holder.commenterProfilePicture.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewProfileActivity.class);
            intent.putExtra("user", comment.getUser());
            context.startActivity(intent);
        });
        holder.commentorName.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewProfileActivity.class);
            intent.putExtra("user", comment.getUser());
            context.startActivity(intent);
        });
        if(comment.getUser().getId() == preferences.getInt("id",0)){
            holder.btnPostOption.setVisibility(View.VISIBLE);
            holder.btnPostOption.setOnClickListener(v -> {
                PopupMenu menuComment = new PopupMenu(context, holder.btnPostOption);
                menuComment.getMenuInflater().inflate(R.menu.menu_comment, menuComment.getMenu());

                try {
                    Field[] fields = menuComment.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        if ("mPopup".equals(field.getName())) {
                            field.setAccessible(true);
                            Object menuPopupHelper = field.get(menuComment);
                            Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                            Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                            setForceIcons.invoke(menuPopupHelper, true);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                menuComment.setOnMenuItemClickListener(item -> {
                    switch(item.getItemId()){

                        case R.id.item_delete:
                            String message, title;
                            if(layout == R.layout.layout_account_comment){
                                message = "Are you sure you want to delete this review ?";
                                title = "Delete Review";
                            }
                            else{
                                message = "Are you sure you want to delete this comment ?";
                                title = "Delete Comment";
                            }
                            new AlertDialog.Builder(context)
                                    .setTitle(title)
                                    .setMessage(message)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String uri;
                                            if(layout == R.layout.layout_account_comment){
                                                uri = Constant.DELETE_COMMENT;
                                            }
                                            else{
                                                uri = Constant.DELETE_POST_COMMENT;
                                            }
                                                StringRequest request = new StringRequest(Request.Method.GET, uri,response -> {
                                                try {
                                                    JSONObject object = new JSONObject(response);
                                                    if (object.has("success")){
                                                        if(layout == R.layout.layout_account_comment){
                                                            Toast.makeText(context, "Review Deleted Successfully", Toast.LENGTH_SHORT).show();
                                                        }else{
                                                            Toast.makeText(context, "Comment Deleted Successfully", Toast.LENGTH_SHORT).show();
                                                        }

                                                    }
                                                    else if(object.has("error")){
                                                        Toast.makeText(context, object.getString("error"), Toast.LENGTH_SHORT).show();

                                                    }
                                                    else
                                                        Toast.makeText(context, "idk!", Toast.LENGTH_SHORT).show();
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();

                                                }

                                            }, Throwable::printStackTrace){
                                                @Override
                                                public Map<String, String> getHeaders() {
                                                    HashMap<String,String> map = new HashMap<>();
//                                                    map.put("comment_id", String.valueOf(comment.getId()));
                                                    map.put("Authorization", "Bearer "+comment.getId());
                                                    return map;
                                                }
                                            };
                                            RequestQueue queue = Volley.newRequestQueue(context);
                                            queue.add(request);

                                        }
                                    })
                                    .setNegativeButton(android.R.string.no,null)
                                    .show();

                            break;
                    }
                    return true;
                });
                menuComment.show();
            });
        }


    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    static class AccountCommentHolder extends RecyclerView.ViewHolder{

        private TextView commentorName,comment,txtCommentDate;
        private CircleImageView commenterProfilePicture;
        private ImageButton btnPostOption;
        private RatingBar ratingBar;
        public AccountCommentHolder(@NonNull View itemView) {
            super(itemView);
            commentorName = itemView.findViewById(R.id.commenterName);
            comment = itemView.findViewById(R.id.comment);
            txtCommentDate = itemView.findViewById(R.id.txtCommentDate);
            commenterProfilePicture = itemView.findViewById(R.id.commenterProfilePicture);
            btnPostOption = itemView.findViewById(R.id.btnPostOption);
            ratingBar = itemView.findViewById(R.id.comment_rating_bar);



        }

    }
}
