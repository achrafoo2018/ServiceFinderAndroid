package com.example.servicefinder;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.servicefinder.Adapters.AccountCommentAdapter;
import com.example.servicefinder.Fragments.ReviewAlertDialogFragment;
import com.example.servicefinder.Models.Comment;
import com.example.servicefinder.Models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends AppCompatActivity {

    private View view;
    private MaterialToolbar toolbar;
    private CircleImageView imgProfile, userImg;
    private TextView txtName,speciality,phone_number,email,description, totalReviews, avgRating, txtRateThis;
    private Button btnEditAccount,btnContact,btnDial;
    private ImageView btnComment;
    private SwipeRefreshLayout refreshLayout,swipeProfile2;
    private RecyclerView recyclerView;
    private ArrayList<Comment> arrayList;
    private SharedPreferences preferences;
    private AccountCommentAdapter adapter;
    private String imgUrl = "";
    private EditText txtComment;
    private User commenter;
    private SlidrInterface slidr;
    private SharedPreferences userPref;
    private RatingBar rBar, avgBar;
    private Toolbar profileToolBar;
    private LinearLayout commentLinearLayout,specialityLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        slidr = Slidr.attach(this);
        init();
    }

    private void init() {
        preferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        toolbar = findViewById(R.id.toolbarAccount);
//        ViewProfileActivity.this.setSupportActionBar(toolbar);
//        setHasOptionsMenu(true);
        commentLinearLayout = findViewById(R.id.commentLinearLayout);
        imgProfile = findViewById(R.id.imgAccountProfile);
        txtName = findViewById(R.id.txtAccountName);
        recyclerView = findViewById(R.id.recyclerAccount);
        swipeProfile2 = findViewById(R.id.swipeProfile2);
        speciality = findViewById(R.id.speciality);
        phone_number = findViewById(R.id.phone_number);
        email = findViewById(R.id.email);
        description = findViewById(R.id.description);
        btnComment = findViewById(R.id.btnComment);
        txtComment = findViewById(R.id.txtComment);
        specialityLayout = findViewById(R.id.specialityLayout);
        btnContact = findViewById(R.id.btnContact);
        btnDial = findViewById(R.id.btnDial);
        txtRateThis = findViewById(R.id.txtRateThis);

        commenter = (User) getIntent().getSerializableExtra("user");

        txtRateThis.setText("Rate this " + commenter.getType().toLowerCase());
        if(commenter.getId() == preferences.getInt("id",0)){
            commentLinearLayout.setVisibility(View.GONE);
        }
        if(commenter.getType().toLowerCase().equals("client")){
            specialityLayout.setVisibility(View.GONE);
            description.setVisibility(View.GONE);
        }

        btnContact.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"+commenter.getEmail())); // only email apps should handle this
            intent.putExtra(Intent.EXTRA_SUBJECT, "Service Finder");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        });
        btnDial.setOnClickListener(v->{

            String phone = commenter.getPhone_number();
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.fromParts("tel", phone, null));
            startActivity(intent);

        });


        userImg = findViewById(R.id.currentUserImgProfile);
        userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        rBar = (RatingBar) findViewById(R.id.rating_bar);
        profileToolBar = findViewById(R.id.toolbarViewProfile);
        profileToolBar.setTitle(commenter.getFirst_name()+" "+commenter.getLast_name()+"'s Profile");
        setSupportActionBar(profileToolBar);
        avgBar = (RatingBar) findViewById(R.id.rating_bar_moy);
        totalReviews = findViewById(R.id.numberReviews);
        avgRating = findViewById(R.id.rating_moy);
        recyclerView.setVisibility(View.VISIBLE);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ViewProfileActivity.this));

        getData();
        getUserRating();

        swipeProfile2.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
                getUserRating();
            }
        });


        //Commenting on profile

        btnComment.setOnClickListener(v -> {

            if(!txtComment.getText().toString().equals("")) {

                String newComment = txtComment.getText().toString().trim();

                StringRequest request = new StringRequest(Request.Method.POST, Constant.CREATE_COMMENT, response -> {

                    try {

                        JSONObject object = new JSONObject(response);
                        if (object.has("success")) {
                        } else if (object.has("error")) {
                            swipeProfile2.setRefreshing(false);
//                        Toast.makeText(ViewProfileActivity.this, object.getString("error"), Toast.LENGTH_LONG).show();
                            if (object.has("counter")) {

                                JSONArray cArrayObject = object.getJSONArray("comment");
                                JSONObject cObject = cArrayObject.getJSONObject(0);
                                Comment c = new Comment();
                                c.setId(cObject.getInt("id"));
                                c.setComment(cObject.getString("comment"));
                                c.setRating(cObject.getInt("rating"));
                                c.setCommenterName(object.getJSONObject("user").getString("first_name") + " " + object.getJSONObject("user").getString("last_name"));
                                PrettyTime p = new PrettyTime();
                                String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
                                DateFormat formatter = new SimpleDateFormat(DEFAULT_PATTERN);
                                String created_at = cObject.getString("created_at");
                                try {
                                    created_at = p.format(formatter.parse(cObject.getString("created_at")));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                c.setCommentDate(created_at);
                                c.setCommenterProfilePicture(object.getJSONObject("user").getString("profile_picture"));

                                DialogFragment reviewDialog = new ReviewAlertDialogFragment(c);
                                reviewDialog
                                        .show(getSupportFragmentManager(), "Review");
                                txtComment.setText(newComment);
                            }
                        }
                    } catch (JSONException e) {
                        Toast.makeText(ViewProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }, error -> {
                    Toast.makeText(ViewProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                }) {

                    @Override
                    public Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("user_id", String.valueOf(preferences.getInt("id", 0)));
                        map.put("provider_id", String.valueOf(commenter.getId()));
                        map.put("comment", txtComment.getText().toString().trim());
                        map.put("rating", String.valueOf(rBar.getRating()));
                        return map;
                    }

                };

                RequestQueue queue = Volley.newRequestQueue(ViewProfileActivity.this);
                queue.add(request);
                txtComment.setText("");
                swipeProfile2.post(() -> {
                    swipeProfile2.setRefreshing(true);
                    getData();
                    getUserRating();
                    swipeProfile2.setRefreshing(false);
                });
                // closing keyboard after clicking on comment
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(swipeProfile2.getWindowToken(), 0);
            }
        });

        //Commenting on profile

    }
    private void getUserRating(){
        StringRequest request = new StringRequest(Request.Method.GET, Constant.USER_RATING, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")) {
                    float AvgRating;
                    if(object.getString("rate").equals("null")){
                        AvgRating = 0;
                    }else{
                        AvgRating = (float) (Math.round(Float.parseFloat(object.getString("rate")) * 10.0) / 10.0);
                    }
                    int totalRev = object.getInt("total");
                    avgBar.setRating(AvgRating);
                    totalReviews.setText("(" + totalRev + " review"+(totalRev == 1 ? "":"s")+")");
                    avgRating.setText(""+AvgRating);

                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        },error -> {
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization","Bearer "+ commenter.getId());
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }
    private void getData() {
        arrayList = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.GET, Constant.COMMENTS_ON_MY_PROFILE, response -> {

            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")){
                    JSONArray comments = object.getJSONArray("comments");
                    JSONObject userObject = object.getJSONObject("user");

                    User user = new User();
                    user.setId(userObject.getInt("id"));
                    user.setFirst_name(userObject.getString("first_name"));
                    user.setLast_name(userObject.getString("last_name"));
                    user.setPhoto(userObject.getString("profile_picture"));
                    user.setType(userObject.getString("type"));
                    user.setPhone_number(userObject.getString("phone_number"));
                    user.setEmail(userObject.getString("email"));
                    if(object.has("provider")){
                        JSONObject providerObject = object.getJSONObject("provider");
                        user.setDescription(providerObject.getString("description"));
                        user.setSpeciality(providerObject.getString("speciality"));
                        if(user.getSpeciality().equals("null")){
                            speciality.setText("");

                        }
                        else{
                            speciality.setText(" " + user.getSpeciality());
                        }
                        if(user.getDescription().equals("null")){
                            description.setText("");
                        }
                        else{
                            description.setText(" " + user.getDescription());

                        }
                    }

                    txtName.setText(user.getFirst_name()+" "+user.getLast_name());
                    //                    Rating here idk
                    Picasso.get().load(Constant.URL+preferences.getString("profile_picture", "")).into(userImg);

                    if(user.getEmail().equals("null")){
                        email.setText("");
                    }
                    else{
                        email.setText(" " + user.getEmail());
                    }
                    if(user.getPhone_number().equals("null")){
                        phone_number.setText("");
                    }
                    else{
                        String phoneNb = user.getPhone_number();
                        if(phoneNb.length() == 8){
                            phoneNb = phoneNb.substring(0, 2) + " " + phoneNb.substring(2, 5) + " " + phoneNb.substring(5);
                        }
                        phone_number.setText(" " + phoneNb);
                    }

                    if (comments.length() != 0) {
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                    for (int i = 0; i < comments.length(); i++){
                        JSONObject c = comments.getJSONObject(i);
                        Comment comment = new Comment();
                        comment.setId(c.getInt("id"));
                        comment.setComment(c.getString("comment"));
                        comment.setRating(c.getInt("rating"));
                        comment.setCommenterName(c.getJSONObject("user").getString("first_name")+" "+c.getJSONObject("user").getString("last_name"));
                        User user_ = new User();
                        JSONObject userObj = c.getJSONObject("user");
                        user_.setId(userObj.getInt("id"));
                        user_.setFirst_name(userObj.getString("first_name"));
                        user_.setLast_name(userObj.getString("last_name"));
                        user_.setPhoto(userObj.getString("profile_picture"));
                        user_.setType(userObj.getString("type"));
                        user_.setPhone_number(userObj.getString("phone_number"));
                        user_.setEmail(userObj.getString("email"));
                        comment.setUser(user_);
                        PrettyTime p = new PrettyTime();
                        String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
                        DateFormat formatter = new SimpleDateFormat(DEFAULT_PATTERN);
                        String created_at = c.getString("created_at");
                        try{
                            created_at = p.format(formatter.parse(c.getString("created_at")));
                        }catch(ParseException e){
                            e.printStackTrace();
                        }
                        comment.setCommentDate(created_at);
                        comment.setCommenterProfilePicture(c.getJSONObject("user").getString("profile_picture"));
                        arrayList.add(comment);

                    }
                    Picasso.get().load(Constant.URL+userObject.getString("profile_picture")).into(imgProfile);
                    adapter = new AccountCommentAdapter(ViewProfileActivity.this,arrayList, R.layout.layout_account_comment);
                    recyclerView.setAdapter(adapter);
                    imgUrl = Constant.URL+userObject.getString("profile_picture");


                }
                else if (object.has("error")){
                    Toast.makeText(ViewProfileActivity.this, object.getString("error"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(ViewProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            swipeProfile2.setRefreshing(false);


        }, error -> {
            Toast.makeText(ViewProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            swipeProfile2.setRefreshing(false);


        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String email = commenter.getEmail();
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization","Bearer "+email);
                return map;
            }

        };

        RequestQueue queue = Volley.newRequestQueue(ViewProfileActivity.this);
        queue.add(request);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}