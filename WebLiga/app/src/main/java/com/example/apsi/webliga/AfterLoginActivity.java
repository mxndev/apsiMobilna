package com.example.apsi.webliga;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AfterLoginActivity extends AppCompatActivity {

    String messageName, messageSurname, userLogin;
    HttpContext localContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_login);

        //pobranie wiadomosci od MainActivity z danymi użytkownika
        final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();
        localContext = globalActivity.getLocalContext();
        userLogin = globalActivity.getUserLogin();

        new ExecuteEnter(localContext,userLogin).execute();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Utworzenie widoku tekstu
        String message = "Witaj " + messageName + " " + messageSurname;
        TextView textView = (TextView) findViewById(R.id.textView2);
        if (textView != null) {
            textView.setText(message);
            textView.setTextSize(40);
        }

    }



    class ExecuteEnter extends AsyncTask<Void, Void, String> {

        HttpContext localContext;
        String login;

        public ExecuteEnter(HttpContext localContext_, String login_)
        {
            localContext = localContext_;
            login = login_;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet httpget = new HttpGet("http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/getUser?login="+login);

                // Pass local context as a parameter
                HttpResponse response2 = client.execute(httpget, localContext);
                String json="", line;
                BufferedReader rd = new BufferedReader(new InputStreamReader(response2.getEntity().getContent(), "UTF-8"));
                while ((line = rd.readLine()) != null) {
                    json += line;
                }
                rd.close();
                JSONObject jsonObject = new JSONObject(json);
                messageName = jsonObject.getString("name");
                messageSurname = jsonObject.getString("surname");

                return json;
            }
            catch(Exception e)
            {
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // w result typu user json
            super.onPostExecute(result);
        }
    }

}



