package com.example.apsi.webliga;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.app.AlertDialog;
import android.content.DialogInterface;
import org.apache.http.HttpResponse;
import org.apache.http.client.*;
import org.apache.http.client.protocol.*;
import org.apache.http.protocol.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.NameValuePair;

import java.util.*;
import org.apache.http.message.*;
import org.apache.http.client.entity.*;
import android.widget.*;

import android.os.*;
import android.content.*;

import static com.example.apsi.webliga.R.*;

public class MainActivity extends AppCompatActivity  {

    CookieStore cookieStore;
    String userLogin;
    HttpContext localContext;
    TextView userJSON;

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
        userJSON = (TextView) findViewById(R.id.userJSON);
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


    public void login(final View view) {

        class ExecuteLogin extends AsyncTask<Void, Void, String> {



            AlertDialog.Builder builder;
            HttpContext localContext;
            String login, password;

            public ExecuteLogin(HttpContext localContext_, String login_, String password_) {
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
                String stringResponse;
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpPost post = new HttpPost("http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/restlogin");

                    ArrayList<NameValuePair> pairs = new ArrayList<>();
                    pairs.add(new BasicNameValuePair("j_login", login));
                    pairs.add(new BasicNameValuePair("j_password", password));
                    post.setEntity(new UrlEncodedFormEntity(pairs));
                    HttpResponse response = client.execute(post, localContext);
                    if (response.getStatusLine().getStatusCode() == 401) {
                        stringResponse = "Error";
                    } else {
                        stringResponse = "OK";
                    }

                    // Pass local context as a parameter
                } catch (Exception e) {
                    return e.toString();
                }
                return stringResponse;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result.equals("OK")) {
                    //pobieranie z globalnych zmiennych loginu oraz cookies
                    final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();
                    globalActivity.setLocalContext(localContext);
                    globalActivity.setUserLogin(userLogin);
                    //uruchomienie drugiej aktywności
                    Intent intent = new Intent(view.getContext(), AfterLoginActivity.class);
                    startActivity(intent);

                } else {
                    builder.setMessage("Błędne dane logowania!")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    EditText editText = (EditText) findViewById(id.loginLoguj);
                                    EditText editText2 = (EditText) findViewById(id.hasloLoguj);
                                    if (editText != null) {
                                        editText.setText("");
                                    }
                                    if (editText2 != null) {
                                        editText2.setText("");
                                    }
                                }
                            })
                            .show();
                }
                super.onPostExecute(result);
            }
        }

        EditText editText = (EditText) findViewById(id.hasloLoguj);
        if (editText != null) {

        }
        EditText editText2 = (EditText) findViewById(id.loginLoguj);

        if (editText2 != null) {
            userLogin = editText2.getText().toString().trim();
            new ExecuteLogin(localContext, userLogin, editText.getText().toString().trim()).execute();
        }
    }
    public void anuluj(final View view) {
        startActivity(new Intent(MainActivity.this, MainWindowActivity.class));
    }
}


