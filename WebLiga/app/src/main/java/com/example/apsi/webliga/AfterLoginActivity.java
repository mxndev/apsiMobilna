package com.example.apsi.webliga;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class AfterLoginActivity extends AppCompatActivity {

    String messageName, messageSurname, userLogin, message;
    HttpContext localContext;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_login);

        final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();
        localContext = globalActivity.getLocalContext();
        userLogin = globalActivity.getUserLogin();

        new ExecuteEnter(localContext, userLogin).execute();


        //uspienie na poł sekundy zeby unikanc wyscigu -> Mariusz kiedys to zmieni (napewno to zrobi :C)
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        //wywołanie funkcji inicjującej pasek toolbar
        initToolBar();

        final Spinner spinner = (Spinner) findViewById(R.id.spinnerToolbar2);
        //  String[] elementy = {"","Menu", "Wyszukaj ligę", "Wyszukaj zespół","Moje mecze"};
        ArrayList<String> arrayList1 = new ArrayList<String>();
        //elementy menu, które będą nie zależnie od typu użytkownika; z jakiegos powodu zapetlenie aktywnosci
        arrayList1.add("Menu");
        arrayList1.add("Menu");
        arrayList1.add("Wyszukaj ligę");
        arrayList1.add("Wyszukaj zespół");
        arrayList1.add("Moje mecze");
        arrayList1.add("Moje zespoły");
        // ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, arrayList1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, arrayList1) {

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = null;

                // If this is the initial dummy entry, make it hidden
                if (position == 0) {
                    TextView tv = new TextView(getContext());
                    tv.setHeight(0);
                    tv.setVisibility(View.GONE);
                    v = tv;
                } else {
                    // Pass convertView as null to prevent reuse of special case views
                    v = super.getDropDownView(position, null, parent);
                }

                // Hide scroll bar because it appears sometimes unnecessarily, this does not prevent scrolling
                parent.setVerticalScrollBarEnabled(false);
                return v;
            }
        };

        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int id, long position) {

                switch ((int) position) {
                    case 0:
                        break;
                    case 1:
                        //przycisk menu który otwiera główną stronę
                        startActivity(new Intent(AfterLoginActivity.this, AfterLoginActivity.class));
                        spinner.setSelection(0);
                        break;
                    case 2:
                        //przycisk wyszukaj ligę, który otwiera stronę wyszukiwania ligi
                        startActivity(new Intent(AfterLoginActivity.this, SearchLeagueActivity.class));
                        spinner.setSelection(0);
                        break;
                    case 3:
                        //przycisk wyszukaj zespół, który otwiera stronę wyszukiwania zespołu
                        startActivity(new Intent(AfterLoginActivity.this, SearchTeamActivity.class));
                        spinner.setSelection(0);
                        break;
                    case 4:
                        //przycisk do otwierania moich meczów
                        startActivity(new Intent(AfterLoginActivity.this, MojeMeczeActivity.class));
                        spinner.setSelection(0);
                        break;
                    case 5:
                        //przycisk do otwierania moich meczów
                        startActivity(new Intent(AfterLoginActivity.this, MyTeamsActivity.class));
                        spinner.setSelection(0);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        // Utworzenie widoku tekstu
        TextView textView = (TextView) findViewById(R.id.textView2);
        if (textView != null) {
            textView.setText(message);
            textView.setTextSize(40);
        }

        this.pobierzIdSedziego();

    }

    public void initToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbarAfterLogin);
        if (toolbar != null) {
            toolbar.setTitle(R.string.toolbarTitle);
        }
    }

    class ExecuteEnter extends AsyncTask<Void, Void, String> {

        HttpContext localContext;
        String login;

        public ExecuteEnter(HttpContext localContext_, String login_) {
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
                HttpGet httpget = new HttpGet("http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/getUser?login=" + login);

                // Pass local context as a parameter
                HttpResponse response2 = client.execute(httpget, localContext);
                String json = "", line;
                BufferedReader rd = new BufferedReader(new InputStreamReader(response2.getEntity().getContent(), "UTF-8"));
                while ((line = rd.readLine()) != null) {
                    json += line;
                }
                rd.close();
                JSONObject jsonObject = new JSONObject(json);
                messageName = jsonObject.getString("name");
                messageSurname = jsonObject.getString("surname");
                message = "Witaj " + messageName + " " + messageSurname;

                //do przechowywania info czy jest to gracz,sedzia,organizaot, kapitan
                final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();
                globalActivity.setIsReferee(jsonObject.getString("isReferee"));
                globalActivity.setIsPlayer(jsonObject.getString("isPlayer"));
                globalActivity.setIsCapitan(jsonObject.getString("isCapitan"));
                globalActivity.setIsOrganizer(jsonObject.getString("isOrganizer"));
                globalActivity.setUserID(jsonObject.getInt("id"));

                return json;
            } catch (Exception e) {
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // w result typu user json
            super.onPostExecute(result);
        }
    }

    public void wyloguj(final View view) {
        startActivity(new Intent(AfterLoginActivity.this, MainWindowActivity.class));
        this.finish();
    }

    public void pobierzIdSedziego() {
        class getRefereeByUser extends AsyncTask<Void, Void, String> {
            private int userID;
            private final String urlToGetLeagueByName =
                    "http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/referee/getRefereeByUser?id=";

            public getRefereeByUser(int userID_) {
                userID = userID_;
            }

            @Override
            protected String doInBackground(Void... params) {
                String napis = Integer.toString(userID);
                String response = null;
                final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet(urlToGetLeagueByName + napis);
                    HttpResponse httpResponse = client.execute(request);
                    InputStream inputStream = httpResponse.getEntity().getContent();

                    if (inputStream != null)
                        response = convertInputStreamToString(inputStream);
                    else
                        response = "Did not work!";

                    JSONObject jsonObject = new JSONObject(response);
                    globalActivity.setRefereeID(jsonObject.getInt("id"));

                } catch (Exception e) {
                    Log.d("InputStream", e.getLocalizedMessage());
                }
                return response;
            }

            @Override
            protected void onPostExecute(String result) {
            }

        }
        final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();
        new getRefereeByUser(globalActivity.getUserID()).execute();
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

}



