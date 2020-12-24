package com.example.servicefinder.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.servicefinder.Constant;
import com.example.servicefinder.Models.Post;
import com.example.servicefinder.R;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AccountPostAdapter extends RecyclerView.Adapter<AccountPostAdapter.AccountPostHolder> {

    private Context context;
    private ArrayList<Post> arrayList;

    public AccountPostAdapter(Context context, ArrayList<Post> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }





    @NonNull
    @Override
    public AccountPostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_account_post, parent, false);
        return new AccountPostHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountPostAdapter.AccountPostHolder holder, int position) {

        Picasso.get().load(arrayList.get(position).getPhoto()).into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class AccountPostHolder extends RecyclerView.ViewHolder{

        private ImageView imageView;

        public AccountPostHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgAccountPost);
        }

    }
}
