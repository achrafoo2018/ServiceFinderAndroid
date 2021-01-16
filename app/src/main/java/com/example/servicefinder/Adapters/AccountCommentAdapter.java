package com.example.servicefinder.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.servicefinder.AuthActivity;
import com.example.servicefinder.Constant;
import com.example.servicefinder.HomeActivity;
import com.example.servicefinder.MainActivity;
import com.example.servicefinder.Models.Comment;
import com.example.servicefinder.Models.Post;
import com.example.servicefinder.R;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountCommentAdapter extends RecyclerView.Adapter<AccountCommentAdapter.AccountCommentHolder> {

    private Context context;
    private ArrayList<Comment> arrayList;

    public AccountCommentAdapter(Context context, ArrayList<Comment> arrayList) {
        this.context = context;
        this.arrayList = arrayList;

    }

    @NonNull
    @Override
    public AccountCommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_account_post, parent, false);
        return new AccountCommentHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountCommentHolder holder, int position) {
        Comment comment = arrayList.get(position);
        holder.commentorName.setText(comment.getCommenterName());
        holder.comment.setText(comment.getComment());
        holder.txtCommentDate.setText(comment.getCommentDate());
        Picasso.get().load(Constant.URL+comment.getCommenterProfilePicture()).into(holder.commenterProfilePicture);
        SharedPreferences preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        String name = preferences.getString("first_name",null) + " " + preferences.getString("last_name",null);
        if(comment.getCommenterName().equals(name)){
            holder.btnPostOption.setVisibility(View.VISIBLE);
            holder.btnPostOption.setOnClickListener(v -> {
                PopupMenu menuComment = new PopupMenu(context, holder.btnPostOption);
                menuComment.getMenuInflater().inflate(R.menu.menu_comment, menuComment.getMenu());

                menuComment.setOnMenuItemClickListener(item -> {
                    switch(item.getItemId()){

                        case R.id.item_edit:


                            break;
                        case R.id.item_delete:

                            new AlertDialog.Builder(context)
                                    .setTitle("Delete comment")
                                    .setMessage("Are you sure you want to delete this review")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            StringRequest request = new StringRequest(Request.Method.GET, Constant.DELETE_COMMENT,response -> {

                                                try {
                                                    JSONObject object = new JSONObject(response);
                                                    if (object.has("success")){
                                                        Toast.makeText(context, "Comment deleted successfully!", Toast.LENGTH_SHORT).show();
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

        public AccountCommentHolder(@NonNull View itemView) {
            super(itemView);
            commentorName = itemView.findViewById(R.id.commenterName);
            comment = itemView.findViewById(R.id.comment);
            txtCommentDate = itemView.findViewById(R.id.txtCommentDate);
            commenterProfilePicture = itemView.findViewById(R.id.commenterProfilePicture);
            btnPostOption = itemView.findViewById(R.id.btnPostOption);




        }

    }
}
