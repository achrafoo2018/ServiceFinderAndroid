package com.example.servicefinder.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.ocpsoft.prettytime.PrettyTime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.servicefinder.Adapters.PostsAdapter;
import com.example.servicefinder.AddPostActivity;
import com.example.servicefinder.Constant;
import com.example.servicefinder.HomeActivity;
import com.example.servicefinder.Models.Post;
import com.example.servicefinder.Models.User;
import com.example.servicefinder.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {
    private View view;
    public static RecyclerView recyclerView;
    public static ArrayList<Post> arrayList;
    private SwipeRefreshLayout refreshLayout;
    private PostsAdapter postsAdapter;
    private MaterialToolbar toolbar;
    private SharedPreferences sharedPreferences;
    RelativeLayout myLayout =null;
    private ArrayList<String> specialities = new ArrayList<>();
    private SearchableSpinner spinner;
    private ArrayAdapter<String> adapter;
    public HomeFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_home,container,false);
        init();
        return view;
    }

    private void init(){
        sharedPreferences = getContext().getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        recyclerView = view.findViewById(R.id.recyclerHome);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        refreshLayout = view.findViewById(R.id.swipeHome);
        toolbar = view.findViewById(R.id.toolbarHome);
        spinner = view.findViewById(R.id.spinnerHome);
        ((HomeActivity)getContext()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        getSpecialities();
        getPosts();

        refreshLayout.setOnRefreshListener(() -> {
            getPosts();
            spinner.setSelection(specialities.size()-1, false);
        });
    }
    private void getSpecialities(){
        // Spinner
        refreshLayout.setRefreshing(true);
        StringRequest request = new StringRequest(Request.Method.GET,Constant.USED_SPECIALITIES, response ->{
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")) {
                    JSONArray array = new JSONArray(object.getString("specialities"));
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject speciality = array.getJSONObject(i);
                        specialities.add(speciality.getString("speciality"));
                    }
                    specialities.add("Choose Speciality...");
                    adapter=new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, specialities){
                        @Override
                        public int getCount() {
                            // to show hint "Select speciality" and don't able to select
                            return specialities.size()-1;
                        }
                    };;
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                    spinner.setSelection(specialities.size()-1, false);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            getPosts(specialities.get(position));
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                            // sometimes you need nothing here
                        }
                    });
                }
            }catch (JSONException e) {
                e.printStackTrace();
                refreshLayout.setRefreshing(false);

            }
        }, Throwable::printStackTrace);
        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(request);
        refreshLayout.setRefreshing(false);
        // End Spinner
    }
    private void getPosts() {
        arrayList = new ArrayList<>();
        refreshLayout.setRefreshing(true);

        StringRequest request = new StringRequest(Request.Method.GET, Constant.POSTS, response -> {

            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")){
                    JSONArray array = new JSONArray(object.getString("posts"));
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject postObject = array.getJSONObject(i);
                        JSONObject userObject = postObject.getJSONObject("user");
                        JSONArray commentObject = new JSONArray(postObject.getString("comments"));
                        User user = new User();
                        user.setId(userObject.getInt("id"));
                        user.setFirst_name(userObject.getString("first_name"));
                        user.setLast_name(userObject.getString("last_name"));
                        user.setPhoto(userObject.getString("profile_picture"));
                        user.setEmail(userObject.getString("email"));
                        user.setPhone_number(userObject.getString("phone_number"));
                        user.setType(userObject.getString("type"));

                        Post post = new Post();
                        post.setId(postObject.getInt("id"));
                        post.setUser(user);
                        post.setComments(commentObject.length());
                        post.setSpeciality(postObject.getString("speciality"));
                        PrettyTime p = new PrettyTime();
                        String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
                        DateFormat formatter = new SimpleDateFormat(DEFAULT_PATTERN);
                        String created_at = postObject.getString("created_at");
                        try{
                             created_at = p.format(formatter.parse(postObject.getString("created_at")));
                        }catch(ParseException e){
                             e.printStackTrace();
                        }
                        post.setDate(created_at);
                        post.setDesc(postObject.getString("desc"));
                        post.setPost_picture(postObject.getString("post_image"));

                        arrayList.add(post);
                    }

                    postsAdapter = new PostsAdapter(getContext(), arrayList);
                    recyclerView.setAdapter(postsAdapter);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            refreshLayout.setRefreshing(false);

        },error -> {
            error.printStackTrace();
            refreshLayout.setRefreshing(false);
        }){

            // provide token in header

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = sharedPreferences.getString("token","");
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization","Bearer "+token);
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(request);
    }
    private void getPosts(String spec) {
        arrayList = new ArrayList<>();
        refreshLayout.setRefreshing(true);

        StringRequest request = new StringRequest(Request.Method.GET, Constant.POSTS, response -> {

            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")){
                    JSONArray array = new JSONArray(object.getString("posts"));
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject postObject = array.getJSONObject(i);
                        JSONObject userObject = postObject.getJSONObject("user");
                        JSONArray commentObject = new JSONArray(postObject.getString("comments"));
                        User user = new User();
                        user.setId(userObject.getInt("id"));
                        user.setFirst_name(userObject.getString("first_name"));
                        user.setLast_name(userObject.getString("last_name"));
                        user.setPhoto(userObject.getString("profile_picture"));
                        user.setEmail(userObject.getString("email"));
                        user.setPhone_number(userObject.getString("phone_number"));
                        user.setType(userObject.getString("type"));

                        Post post = new Post();
                        post.setId(postObject.getInt("id"));
                        post.setUser(user);
                        post.setComments(commentObject.length());
                        PrettyTime p = new PrettyTime();
                        String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
                        DateFormat formatter = new SimpleDateFormat(DEFAULT_PATTERN);
                        String created_at = postObject.getString("created_at");
                        try{
                             created_at = p.format(formatter.parse(postObject.getString("created_at")));
                        }catch(ParseException e){
                             e.printStackTrace();
                        }
                        post.setDate(created_at);
                        post.setSpeciality(postObject.getString("speciality"));
                        post.setDesc(postObject.getString("desc"));
                        post.setPost_picture(postObject.getString("post_image"));
                        if(post.getSpeciality().equals(spec))
                            arrayList.add(post);
                    }

                    postsAdapter = new PostsAdapter(getContext(), arrayList);
                    recyclerView.setAdapter(postsAdapter);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            refreshLayout.setRefreshing(false);

        },error -> {
            error.printStackTrace();
            refreshLayout.setRefreshing(false);
        }){

            // provide token in header

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = sharedPreferences.getString("token","");
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization","Bearer "+token);
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(request);
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search,menu);
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView)item.getActionView();
        searchView.setQueryHint("Search...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                postsAdapter.getFilter().filter(newText, count -> {
                    if (count == 0){
                        TextView textView = view.findViewById(R.id.noDataFound);
                        textView.setVisibility(View.VISIBLE);
                    }
                });

                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

}
