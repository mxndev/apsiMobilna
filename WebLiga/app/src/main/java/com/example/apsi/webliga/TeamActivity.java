package com.example.apsi.webliga;

/**
 * Created by xx on 2016-05-08.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class TeamActivity extends AppCompatActivity {
    ListView listView;
    TeamAdapter teamAdapter;
    private ArrayList<TeamListElement> teamListElements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team);
        listView = (ListView) findViewById(R.id.listView2);
        class RankingExecute extends AsyncTask<Void, Void, String> {
            private int teamID;
            private final String urlToGetLeagueByName =
                    "http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/home/getTeamsById?id=";

            public RankingExecute(int teamID_) {
                teamID = teamID_;
            }

            @Override
            protected String doInBackground(Void... params) {
                String ret;
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet(String.format("%s%d", urlToGetLeagueByName, teamID));
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
            protected void onPostExecute(String result){
                JSONObject jsonObject;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    try {
                        jsonObject = new JSONObject(result);
                        teamListElements = parseJSONObject(jsonObject);
                        teamAdapter = new TeamAdapter(TeamActivity.this, 0, teamListElements);
                        listView.setAdapter(teamAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            private ArrayList<TeamListElement> parseJSONObject(JSONObject jsonObject) throws JSONException {
                ArrayList<TeamListElement> teamListElements = new ArrayList<>();
                String name1 = jsonObject.getJSONObject("capitan").getJSONObject("user").getString("name");
                String surname1 = jsonObject.getJSONObject("capitan").getJSONObject("user").getString("surname");
                teamListElements.add(new TeamListElement(name1, surname1, "Kapitan"));
                String jsonArrayString = jsonObject.getJSONArray("players").toString();
                JSONArray jsonArray = new JSONArray(jsonArrayString);
                int i = 0;
                while (i < jsonArray.length()) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    String name = jsonObject1.getJSONObject("user").getString("name");
                    String surname = jsonObject1.getJSONObject("user").getString("surname");
                    teamListElements.add(new TeamListElement(name, surname, ""));
                    i++;
                }
                return teamListElements;
            }
        }
        Intent intent = getIntent();
        int groupId = intent.getIntExtra("ID", 0);
        new RankingExecute(groupId).execute();
    }
}

class TeamAdapter extends ArrayAdapter<TeamListElement> {
    private ArrayList<TeamListElement> listOfElements;
    private static LayoutInflater inflater = null;

    public TeamAdapter (Activity activity, int textViewResourceId, ArrayList<TeamListElement> element) {
        super(activity, textViewResourceId, element);
        try {
            this.listOfElements = element;
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        } catch (Exception e) {
        }
    }

    @Override
    public int getCount() { return listOfElements.size(); }

    @Override
    public long getItemId(int position) { return position; }

    public static class ViewHolder {
        public TextView namesurname;
        public TextView capitan;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.team_list_element, null);
                holder = new ViewHolder();
                holder.namesurname = (TextView) vi.findViewById(R.id.nameSurnameLabel);
                holder.capitan = (TextView) vi.findViewById(R.id.nameSurnameLabelTV);
                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }
            holder.namesurname.setText(listOfElements.get(position).getName() + " " + listOfElements.get(position).getSurname());
            holder.capitan.setText(listOfElements.get(position).getCapitan());
        } catch (Exception e) { }

        return vi;
    }
}

class TeamListElement {
    private String name, surname, capitan;

    public TeamListElement(String name_, String surname_, String capitan_) {
        name = name_;
        surname = surname_;
        capitan = capitan_;
    }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getCapitan() { return capitan; }
}