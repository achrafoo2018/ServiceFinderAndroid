package com.example.servicefinder.Fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.servicefinder.Constant;
import com.example.servicefinder.Models.Comment;
import com.example.servicefinder.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewAlertDialogFragment extends DialogFragment {

    private Comment c;
    private TextView commenterName,comment,txtCommentDate;
    private CircleImageView commenterProfilePicture;
    private ImageButton btnPostOption;
    private RatingBar ratingBar;
    private LinearLayout reviewDangerLinearLayout;
    private Button btnreviewDangerDelete,btnreviewDangerCancel;
    private String newComment;

    public ReviewAlertDialogFragment(Comment c) {
        this.c = c;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_account_comment, container, false);



        // Do all the stuff to initialize your custom view

        commenterName = v.findViewById(R.id.commenterName);
        comment = v.findViewById(R.id.comment);
        txtCommentDate = v.findViewById(R.id.txtCommentDate);
        commenterProfilePicture = v.findViewById(R.id.commenterProfilePicture);
        ratingBar = v.findViewById(R.id.comment_rating_bar);
        reviewDangerLinearLayout = v.findViewById(R.id.reviewDangerLinearLayout);
        btnreviewDangerDelete = v.findViewById(R.id.btnreviewDangerDelete);
        btnreviewDangerCancel = v.findViewById(R.id.btnreviewDangerCancel);


        btnreviewDangerDelete.setOnClickListener(v1 -> {

            StringRequest request = new StringRequest(Request.Method.GET, Constant.DELETE_COMMENT, response -> {
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.has("success")){
                            Toast.makeText(getContext(), "Review Deleted Successfully", Toast.LENGTH_SHORT).show();
                            this.dismiss();


                    }
                    else if(object.has("error")){
                        Toast.makeText(getContext(), object.getString("error"), Toast.LENGTH_SHORT).show();

                    }
                    else
                        Toast.makeText(getContext(), "idk!", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                }

            }, Throwable::printStackTrace){
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String,String> map = new HashMap<>();
//                                                    map.put("comment_id", String.valueOf(comment.getId()));
                    map.put("Authorization", "Bearer "+c.getId());
                    return map;
                }
            };
            RequestQueue queue = Volley.newRequestQueue(getContext());
            queue.add(request);


        });

        btnreviewDangerCancel.setOnClickListener(v1 -> {
            this.dismiss();
        });

        commenterName.setText(c.getCommenterName());
        comment.setText(c.getComment());
        txtCommentDate.setText(c.getCommentDate());
        Picasso.get().load(Constant.URL+c.getCommenterProfilePicture()).into(commenterProfilePicture);
        ratingBar.setRating(c.getRating());
        reviewDangerLinearLayout.setVisibility(View.VISIBLE);


        return v;
    }

    @Override
    public int getTheme() {
        return R.style.Theme_AppCompat_Light_Dialog_Alert;
    }
}
