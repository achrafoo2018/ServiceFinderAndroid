package com.example.servicefinder;

public class Constant {
    public static final String URL = "http://192.168.1.8:8080/";
    public static final String HOME = URL+"api";
    public static final String LOGIN = HOME+"/login";
    public static final String REGISTER = HOME+"/register";
    public static final String LOGOUT = HOME+"/logout";
    public static final String SENDPASSWORDRESETLINK=HOME+"/sendPasswordResetLink";
    public static final String RESETPASSWORD=HOME+"/resetPassword";
    public static final String SAVE_USER_INFO = HOME+"/updateAccount";
    public static final String POSTS = HOME+"/posts";

    public static final String MY_POST = POSTS+"/myPosts";
    public static final String ADD_POST = POSTS+"/create";

}
