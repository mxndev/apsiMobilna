package com.example.apsi.webliga;

/**
 * Created by xx on 2016-05-07.
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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

public class GroupActivity extends AppCompatActivity {
    ListView listView;
    GroupAdapter groupAdapter;
    private ArrayList<GroupListElement> groupListElements;
    ArrayList<Integer> leaguesReferee = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        listView = (ListView) findViewById(R.id.listView2);

        OnItemClickListener listener = new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Object o = listView.getItemAtPosition(position);
                GroupListElement obj_itemDetails = (GroupListElement) o;
                Intent intent = new Intent(GroupActivity.this, RankingActivity.class);
                intent.putExtra("ID", obj_itemDetails.getGroupID());
                startActivity(intent);
            }
        };
        listView.setOnItemClickListener(listener);

        class GroupExecute extends AsyncTask<Void, Void, String> {
            private int leagueID;
            private final String urlToGetLeagueByName =
                    "http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/home/getGroupsByLeague?id=";

            public GroupExecute(int leagueID_) {
                leagueID = leagueID_;
            }

            @Override
            protected String doInBackground(Void... params) {
                String ret;
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet(String.format("%s%d", urlToGetLeagueByName, leagueID));
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
                        groupListElements = parseJSONArray(jsonArray);
                        groupAdapter = new GroupAdapter(GroupActivity.this, 0, groupListElements);
                        listView.setAdapter(groupAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            private ArrayList<GroupListElement> parseJSONArray(JSONArray jsonArray) throws JSONException {
                int i = 0;
                ArrayList<GroupListElement> groupListElements = new ArrayList<>();
                while (i < jsonArray.length()) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int groupID = jsonObject.getInt("id");
                    String matches = jsonObject.getString("matchesNo");
                    String teamsPlayers = jsonObject.getString("teamsPlayersNo");
                    String promotions = jsonObject.getString("promotionsNo");
                    String phase = jsonObject.getString("phase");
                    groupListElements.add(new GroupListElement(matches, teamsPlayers, promotions, phase, groupID));
                    i++;
                }
                return groupListElements;
            }
        }
        Intent intent = getIntent();
        int groupId = intent.getIntExtra("ID", 0);
        new GroupExecute(groupId).execute();

        this.pobranieLigSedziego();

    }

    public void pobranieLigSedziego() {
        class RefereeLeaguesExecute extends AsyncTask<Void, Void, String> {
            private int refereeID;
            private final String urlToGetLeagueByName =
                    "http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/referee/getLeagues?id=";

            public RefereeLeaguesExecute(int refereeID_) {
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
                        int i = 0;
                        while (i < jsonArray.length()) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Integer id = jsonObject.getInt("id");
                            leaguesReferee.add(id);
                            i++;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();
        new RefereeLeaguesExecute(globalActivity.getRefereeID()).execute();
    }
}

class GroupAdapter extends ArrayAdapter<GroupListElement> {
    private ArrayList<GroupListElement> listOfElements;
    private static LayoutInflater inflater = null;

    public GroupAdapter (Activity activity, int textViewResourceId, ArrayList<GroupListElement> element) {
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
        public TextView matches;
        public TextView teamPlayers;
        public TextView promotions;
        public TextView phase;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.groups_list_element, null);
                holder = new ViewHolder();
                holder.matches = (TextView) vi.findViewById(R.id.matchesLabelTV);
                holder.teamPlayers = (TextView) vi.findViewById(R.id.teamPlayersLabelTV);
                holder.promotions = (TextView) vi.findViewById(R.id.promotionsLabelTV);
                holder.phase = (TextView) vi.findViewById(R.id.phaseLabelTV);
                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }
            holder.matches.setText(listOfElements.get(position).getMatches());
            holder.matches.setTag(listOfElements.get(position).getGroupID());
            holder.teamPlayers.setText(listOfElements.get(position).getTeamPlayers());
            holder.promotions.setText(listOfElements.get(position).getPromotions());
            holder.phase.setText(listOfElements.get(position).getPhase());
        } catch (Exception e) { }

        return vi;
    }
}

class GroupListElement {
    private String matches, teamPlayers, promotions, phase;
    private int groupID;

    public GroupListElement(String matches_, String teamPlayers_, String promotions_, String phase_, int groupID_) {
        matches = matches_;
        teamPlayers = teamPlayers_;
        promotions = promotions_;
        phase = phase_;
        groupID = groupID_;
    }
    public String getMatches() { return matches; }
    public String getTeamPlayers() { return teamPlayers; }
    public String getPromotions() { return promotions; }
    public String getPhase() { return phase; }
    public int getGroupID() { return groupID; }
}