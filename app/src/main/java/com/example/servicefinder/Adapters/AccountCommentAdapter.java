package com.example.servicefinder.Adapters;

import android.content.Context;
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
import com.example.servicefinder.Models.Post;
import com.example.servicefinder.R;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

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
