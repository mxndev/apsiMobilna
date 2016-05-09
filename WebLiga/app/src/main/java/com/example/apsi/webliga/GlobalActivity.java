package com.example.apsi.webliga;

import android.app.Application;

import org.apache.http.protocol.HttpContext;

public class GlobalActivity extends Application {

    private HttpContext localContext;
    private String userLogin,isReferee,isCapitan,isPlayer,isOrganizer;

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

    public String getIsReferee(){
        return isReferee;
    }
    public void setIsReferee(String aisReferee){
        this.isReferee=aisReferee;
    }

    public String getIsCapitan(){
        return isCapitan;
    }
    public void setIsCapitan(String aisCapitan){
        this.isCapitan=aisCapitan;
    }

    public String getIsPlayer(){
        return isPlayer;
    }
    public void setIsPlayer(String aisPlayer){
        this.isPlayer=aisPlayer;
    }

    public String getIsOrganizer(){
        return isOrganizer;
    }
    public void setIsOrganizer(String aisOrganizer){
        this.isOrganizer=aisOrganizer;
    }

}
