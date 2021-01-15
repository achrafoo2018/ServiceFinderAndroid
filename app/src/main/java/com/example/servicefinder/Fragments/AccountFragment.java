package com.example.servicefinder.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.servicefinder.Adapters.AccountCommentAdapter;
import com.example.servicefinder.AuthActivity;
import com.example.servicefinder.Constant;
import com.example.servicefinder.EditUserInfoActivity;
import com.example.servicefinder.HomeActivity;
import com.example.servicefinder.Models.Comment;
import com.example.servicefinder.Models.Post;
import com.example.servicefinder.Models.Provider;
import com.example.servicefinder.Models.User;
import com.example.servicefinder.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
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

public class AccountFragment extends Fragment {

    private View view;
    private MaterialToolbar toolbar;
    private CircleImageView imgProfile;
    private TextView txtName, txtPostsCount,service,speciality,phone_number,description;
    private ImageView btnComment;
    private SwipeRefreshLayout swipeProfile2;
    private RecyclerView recyclerView;
    private ArrayList<Comment> arrayList;
    private SharedPreferences preferences;
    private AccountCommentAdapter adapter;
    private String imgUrl = "";
    private EditText txtComment;
    private LinearLayout commentLayout;
    private NestedScrollView profileScrollView;

    public AccountFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_account, container, false);
        init();
        return view;
    }

    private void init() {
        preferences = getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        toolbar = view.findViewById(R.id.toolbarAccount);
        ((HomeActivity)getContext()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        imgProfile = view.findViewById(R.id.imgAccountProfile);
        txtName = view.findViewById(R.id.txtAccountName);
        txtPostsCount = view.findViewById(R.id.txtAccountPostCount);
        recyclerView = view.findViewById(R.id.recyclerAccount);
        swipeProfile2 = view.findViewById(R.id.swipeProfile2);
        service = view.findViewById(R.id.service);
        speciality = view.findViewById(R.id.speciality);
        phone_number = view.findViewById(R.id.phone_number);
        description = view.findViewById(R.id.description);
        btnComment = view.findViewById(R.id.btnComment);
        txtComment = view.findViewById(R.id.txtComment);
        commentLayout = view.findViewById(R.id.commentLayout);
        profileScrollView = view.findViewById(R.id.profileScrollView);

        if(preferences.getString("type","").equals("Provider")){
            recyclerView.setVisibility(View.VISIBLE);
            commentLayout.setVisibility(View.VISIBLE);
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        getData();

        swipeProfile2.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });


        //Commenting on profile

        btnComment.setOnClickListener(v -> {

            StringRequest request = new StringRequest(Request.Method.POST, Constant.CREATE_COMMENT, response -> {

                swipeProfile2.setRefreshing(true);
            },error -> {
            }){

                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String,String> map = new HashMap<>();
                    map.put("user_id", String.valueOf(preferences.getInt("id",0)));
                    map.put("provider_id", String.valueOf(preferences.getInt("id",0)));
                    map.put("comment", txtComment.getText().toString().trim());
                    return map;
                }

            };

            RequestQueue queue = Volley.newRequestQueue(getContext());
            queue.add(request);
            txtComment.setText("");
            swipeProfile2.post(() -> {
                swipeProfile2.setRefreshing(true);
                getData();
                swipeProfile2.setRefreshing(false);
            });
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(swipeProfile2.getWindowToken(), 0);

        });

        //Commenting on profile

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

                    txtName.setText(user.getFirst_name()+" "+user.getLast_name());
                    //                    Rating here idk

                    if(preferences.getString("service","").equals("null")){
                        service.setText("");
                    }
                    else{
                        service.setText(" " + preferences.getString("service",""));
                    }
                    if(preferences.getString("speciality","").equals("null")){
                        speciality.setText("");

                    }
                    else{
                        speciality.setText(" " + preferences.getString("speciality",""));
                    }
                    if(preferences.getString("phone_number","").equals("null")){
                        phone_number.setText("");
                    }
                    else{
                        phone_number.setText(" " + preferences.getString("phone_number",""));
                    }
                    if(preferences.getString("description","").equals("null")){
                        description.setText("");
                    }
                    else{
                        description.setText(" " + preferences.getString("description",""));

                    }

                    if (comments.length() != 0)
                        recyclerView.setVisibility(View.VISIBLE);
                    for (int i = 0; i < comments.length(); i++){
                        JSONObject c = comments.getJSONObject(i);
                        Comment comment = new Comment();
                        comment.setComment(c.getString("comment"));
                        comment.setCommenterName(c.getJSONObject("user").getString("first_name")+" "+c.getJSONObject("user").getString("last_name"));
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
                    adapter = new AccountCommentAdapter(getContext(),arrayList);
                    recyclerView.setAdapter(adapter);
                    imgUrl = Constant.URL+userObject.getString("profile_picture");


                }
                else if (object.has("error")){
                    Toast.makeText(getActivity(), object.getString("error"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            swipeProfile2.setRefreshing(false);


        }, error -> {
            Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            swipeProfile2.setRefreshing(false);


        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String email = preferences.getString("email", null);
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization","Bearer "+email);
                return map;
            }

        };

        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(request);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_account, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.item_logout: {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Do you want to logout?");
                builder.setPositiveButton("Logout", (dialog, which) -> logout());
                builder.setNegativeButton("Cancel", (dialog, which) -> {

                });
                builder.show();
                break;
            }
            case R.id.item_account_settings: {
                Intent intent = new Intent((HomeActivity)getContext(), EditUserInfoActivity.class);
                intent.putExtra("imgUrl", imgUrl);
                startActivity(intent);

                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout(){
        StringRequest request = new StringRequest(Request.Method.GET, Constant.LOGOUT,response -> {

            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")){
                    Toast.makeText(getActivity(), "Logging out!", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.apply();
                    startActivity(new Intent((HomeActivity)getContext(), AuthActivity.class));
                    ((HomeActivity)getContext()).finish();
                }
                else if(object.has("error")){
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.apply();
                    startActivity(new Intent((HomeActivity)getContext(), AuthActivity.class));
                    ((HomeActivity)getContext()).finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, Throwable::printStackTrace){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = preferences.getString("token", "");
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization", "Bearer "+token);
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(request);

    }
}