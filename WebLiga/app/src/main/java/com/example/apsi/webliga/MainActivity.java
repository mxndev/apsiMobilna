package com.example.apsi.webliga;

import android.app.Service;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.app.AlertDialog;
import android.content.DialogInterface;
import java.io.InputStream;
import org.apache.http.client.methods.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.*;
import org.apache.http.client.protocol.*;
import org.apache.http.protocol.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.client.DefaultHttpClient;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.NameValuePair;
import java.util.*;
import org.apache.http.message.*;
import org.apache.http.client.entity.*;
import org.json.*;
import android.widget.*;

import android.os.*;
import android.content.*;

import static com.example.apsi.webliga.R.*;

public class MainActivity extends AppCompatActivity {

    CookieStore cookieStore;
    String userLogin;
    HttpContext localContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(id.toolbar);
        setSupportActionBar(toolbar);
        cookieStore = new BasicCookieStore();
        // Create local HTTP context
        localContext = new BasicHttpContext();
        // Bind custom cookie store to the local context
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public void login(View view) {

        class ExecuteLogin extends AsyncTask<Void, Void, String> {

            private Exception exception = null;

            AlertDialog.Builder builder;
            HttpContext localContext;
            String login, password;

            public ExecuteLogin(HttpContext localContext_, String login_, String password_)
            {
                localContext = localContext_;
                login = login_;
                password = password_;
                builder = new AlertDialog.Builder(MainActivity.this);
            }

            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(Void... params) {
                String stringResponse = "", resolve2 = "yoł";
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpPost post = new HttpPost("http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/restlogin");

                    List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                    pairs.add(new BasicNameValuePair("j_login", login));
                    pairs.add(new BasicNameValuePair("j_password", password));
                    post.setEntity(new UrlEncodedFormEntity(pairs));
                    HttpResponse response = client.execute(post, localContext);
                    if(response.getStatusLine().getStatusCode() == 401)
                    {
                        stringResponse = "Error";
                    } else {
                        stringResponse = "OK";
                    }

                    HttpGet httpget = new HttpGet("http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/getUser?login=aaa");

                    // Pass local context as a parameter
                    HttpResponse response2 = client.execute(httpget, localContext);
                    String json="", line;
                    BufferedReader rd = new BufferedReader(new InputStreamReader(response2.getEntity().getContent(), "UTF-8"));
                    while ((line = rd.readLine()) != null) {
                        json += line;
                    }
                    rd.close();
                    JSONObject jsonObject = new JSONObject(json);
                    resolve2 = json;
                }
                catch(Exception e)
                {
                    return e.toString();
                }
                return stringResponse;
            }

            @Override
            protected void onPostExecute(String result) {
                if(result == "OK")
                {
                    // tutaj wrzucić ładowanie nowego ekranu + dane z getUser z jsona, alerta wywalić
                    builder.setMessage("Dane poprawne! Zalogowano!")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    // Some stuff to do when ok got clicked
                                }
                            })
                            .show();
                } else {
                    builder.setMessage("Błędne dane logowania!")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    // Some stuff to do when ok got clicked
                                }
                            })
                            .show();
                }
                super.onPostExecute(result);
            }
        }

        EditText editText = (EditText)findViewById(id.editText);
        userLogin = editText.getText().toString().trim();
        EditText editText2 = (EditText)findViewById(id.editText2);

        new ExecuteLogin(localContext, userLogin, editText2.getText().toString().trim()).execute();
    }

    class ExecuteEnter extends AsyncTask<Void, Void, String> {

        AlertDialog.Builder builder;
        HttpContext localContext;
        String login, password;

        public ExecuteEnter(HttpContext localContext_, String login_)
        {
            localContext = localContext_;
            login = login_;
            builder = new AlertDialog.Builder(MainActivity.this);
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
