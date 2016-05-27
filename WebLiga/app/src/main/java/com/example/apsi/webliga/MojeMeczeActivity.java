package com.example.apsi.webliga;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MojeMeczeActivity extends AppCompatActivity {

    Toolbar toolbar;
    ListView listView;
    WynikiAdapter wynikiAdapter;
    ArrayList<WynikiListElement> wynikiListElements;
    int sedziaID;
    String response;
    boolean click = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moje_mecze);
        initToolBar();
        listView = (ListView) findViewById(R.id.listaMeczySedzia);

        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Object o = listView.getItemAtPosition(position);
                WynikiListElement obj_itemDetails = (WynikiListElement)o;
                Intent intent = new Intent(MojeMeczeActivity.this, ChangeResultsActivity.class);
                intent.putExtra("ID", obj_itemDetails.getId());
                startActivity(intent);
            }
        };
        listView.setOnItemClickListener(listener);

        //do sprawdzenia jakiego typu jest użytkownik
        final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();

        if (globalActivity.getIsCapitan().equals("Y")) {
            final Button buttonKapitan = (Button) findViewById(R.id.buttonKapitan);
            buttonKapitan.setEnabled(true);
        }
        if (globalActivity.getIsPlayer().equals("Y")) {
            final Button buttonGracz = (Button) findViewById(R.id.buttonGracz);
            buttonGracz.setEnabled(true);
        }
        if (globalActivity.getIsReferee().equals("Y")) {
            final Button buttonSedzia = (Button) findViewById(R.id.buttonSedzia);
            buttonSedzia.setEnabled(true);
        }
        if (globalActivity.getIsOrganizer().equals("Y")) {
            final Button buttonOrganizator = (Button) findViewById(R.id.buttonOrganizator);
            buttonOrganizator.setEnabled(true);
        }

        final Spinner spinner = (Spinner) findViewById(R.id.spinnerToolbar2);
        //  String[] elementy = {"","Menu", "Wyszukaj ligę", "Wyszukaj zespół","Moje mecze"};
        ArrayList<String> arrayList1 = new ArrayList<String>();
        //elementy menu, które będą nie zależnie od typu użytkownika; z jakiegos powodu zapetlenie aktywnosci
        arrayList1.add("Menu");
        arrayList1.add("Menu");
        arrayList1.add("Wyszukaj ligę");
        arrayList1.add("Wyszukaj zespół");
        arrayList1.add("Moje mecze");
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
                        startActivity(new Intent(MojeMeczeActivity.this, AfterLoginActivity.class));
                        spinner.setSelection(0);
                        break;
                    case 2:
                        //przycisk wyszukaj ligę, który otwiera stronę wyszukiwania ligi
                        startActivity(new Intent(MojeMeczeActivity.this, SearchLeagueActivity.class));
                        spinner.setSelection(0);
                        break;
                    case 3:
                        //przycisk wyszukaj zespół, który otwiera stronę wyszukiwania zespołu
                        startActivity(new Intent(MojeMeczeActivity.this, SearchTeamActivity.class));
                        spinner.setSelection(0);
                        break;
                    case 4:
                        //przycisk do otwierania moich meczów
                        startActivity(new Intent(MojeMeczeActivity.this, MojeMeczeActivity.class));
                        spinner.setSelection(0);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

    //to utworzenia toolbaru
    public void initToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.toolbarTitle);
        }
    }

    //obsluga przycisku wyloguj
    public void wyloguj(final View view) {
        startActivity(new Intent(MojeMeczeActivity.this, MainActivity.class));
    }

    //obluga przycisku sedzia
    public void sedzia(final View view) {
        this.pobranieMeczowSedziego(view);
    }

    private void pobranieMeczowSedziego(final View view) {
        class MatchExecute extends AsyncTask<Void, Void, String> {
            private int refereeID;
            private final String urlToGetLeagueByName =
                    "http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/referee/getMatches?id=";

            public MatchExecute(int refereeID_) {
                refereeID = refereeID_;
            }

            @Override
            protected String doInBackground(Void... params) {
                String ret;
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet(String.format("%s%d", urlToGetLeagueByName, refereeID));
                    HttpResponse response = client.execute(request);
                    BufferedReader rd = new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }
                    ret = result.toString();
                } catch (Exception e) {
                    return e.toString();
                }
                return ret;
            }

            @Override
            protected void onPostExecute(String result) {
                JSONArray jsonArray;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    try {
                        jsonArray = new JSONArray(result);
                        wynikiListElements = parseJSONArray(jsonArray);
                        wynikiAdapter = new WynikiAdapter(MojeMeczeActivity.this, 0, wynikiListElements);
                        listView.setAdapter(wynikiAdapter);
                        listView.setVisibility(View.VISIBLE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            private ArrayList<WynikiListElement> parseJSONArray(JSONArray jsonArray) throws JSONException {
                int i = 0;
                ArrayList<WynikiListElement> groupListElements = new ArrayList<>();
                while (i < jsonArray.length()) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int id = jsonObject.getInt("id");
                    JSONArray teamArray = jsonObject.getJSONArray("scoreTeam");
                    JSONArray indArray = jsonObject.getJSONArray("scoreInd");
                    String date = jsonObject.getString("date");
                    if (teamArray.length() > 1) {
                        JSONObject teamObject1 = teamArray.getJSONObject(0);
                        String score1 = teamObject1.getString("score");
                        String team1 = teamObject1.getJSONObject("pk").getJSONObject("team").getString("name");
                        JSONObject teamObject2 = teamArray.getJSONObject(1);
                        String score2 = teamObject2.getString("score");
                        String team2 = teamObject2.getJSONObject("pk").getJSONObject("team").getString("name");
                        groupListElements.add(new WynikiListElement(id, date, team1, score1, team2, score2));
                    }
                    if (indArray.length() > 1) {
                        JSONObject teamObject1 = indArray.getJSONObject(0);
                        String score1 = teamObject1.getString("score");
                        String team1 = teamObject1.getJSONObject("pk").getJSONObject("player").getJSONObject("user").getString("name") + " " + teamObject1.getJSONObject("pk").getJSONObject("player").getJSONObject("user").getString("surname");
                        JSONObject teamObject2 = indArray.getJSONObject(1);
                        String score2 = teamObject2.getString("score");
                        String team2 = teamObject2.getJSONObject("pk").getJSONObject("player").getJSONObject("user").getString("name") + " " + teamObject2.getJSONObject("pk").getJSONObject("player").getJSONObject("user").getString("surname");
                        groupListElements.add(new WynikiListElement(id, date, team1, score1, team2, score2));
                    }
                    i++;
                }
                return groupListElements;
            }

        }
        final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();
        new MatchExecute(globalActivity.getRefereeID()).execute();
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
