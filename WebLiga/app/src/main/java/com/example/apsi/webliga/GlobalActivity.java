package com.example.apsi.webliga;

import android.app.Application;

import org.apache.http.protocol.HttpContext;

public class GlobalActivity extends Application {

    private HttpContext localContext;
    private String userLogin,isReferee,isCapitan,isPlayer,isOrganizer;
    private int userID;
    private int refereeID;
    private String leagueName;

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
        if(isReferee == null)
        {
            return null;
        }
        return isReferee;
    }
    public void setIsReferee(String aisReferee){
        this.isReferee=aisReferee;
    }

    public String getIsCapitan(){
        if(isCapitan == null)
        {
            return null;
        }
        return isCapitan;
    }
    public void setIsCapitan(String aisCapitan){
        this.isCapitan=aisCapitan;
    }

    public String getIsPlayer(){
        if(isPlayer == null)
        {
            return null;
        }
        return isPlayer;
    }
    public void setIsPlayer(String aisPlayer){
        this.isPlayer=aisPlayer;
    }

    public String getIsOrganizer(){
        if(isOrganizer == null)
        {
            return null;
        }
        return isOrganizer;
    }
    public void setIsOrganizer(String aisOrganizer){
        this.isOrganizer=aisOrganizer;
    }

    public int getUserID() {
        return userID;
    }
    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void setRefereeID(int refereeID) {
        this.refereeID = refereeID;
    }
    public int getRefereeID() {
        return refereeID;
    }


    public String getLeagueName() {
        return leagueName;
    }

    public void setLeagueName(String leagueName) {
        this.leagueName = leagueName;
    }


}
