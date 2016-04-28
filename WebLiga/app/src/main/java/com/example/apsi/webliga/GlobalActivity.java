package com.example.apsi.webliga;

import android.app.Application;

import org.apache.http.protocol.HttpContext;

public class GlobalActivity extends Application {

    private HttpContext localContext;
    private String userLogin;

    public HttpContext getLocalContext(){
        return localContext;
    }
    public void setLocalContext(HttpContext alocalContext){
        this.localContext=alocalContext;
    }
    public String getUserLogin(){
        return userLogin;
    }
    public void setUserLogin(String auserLogin){
        this.userLogin=auserLogin;
    }

}
