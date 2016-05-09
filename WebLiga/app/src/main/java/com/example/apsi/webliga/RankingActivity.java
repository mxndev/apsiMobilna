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

public class RankingActivity extends AppCompatActivity {
    ListView listView;
    RankingAdapter rankingAdapter;
    int groupId;
    private ArrayList<RankingListElement> rankingListElements;

    public void terminarzCreate(View view)
    {
        Intent intent = new Intent(RankingActivity.this, TerminarzActivity.class);
        intent.putExtra("ID", groupId);
        startActivity(intent);
    }

    public void wynikiCreate(View view)
    {
        Intent intent = new Intent(RankingActivity.this, WynikiActivity.class);
        intent.putExtra("ID", groupId);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
        listView = (ListView) findViewById(R.id.listView2);

        Intent intent = getIntent();
        groupId = intent.getIntExtra("ID", 0);

        OnItemClickListener listener = new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Object o = listView.getItemAtPosition(position);
                RankingListElement obj_itemDetails = (RankingListElement)o;
                Intent intent = new Intent(RankingActivity.this, TeamActivity.class);
                intent.putExtra("ID", obj_itemDetails.getTeamID());
                startActivity(intent);
            }
        };
        listView.setOnItemClickListener(listener);

        class RankingExecute extends AsyncTask<Void, Void, String> {
            private int groupID;
            private final String urlToGetLeagueByName =
                    "http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/home/getGroupRanking?id=";

            public RankingExecute(int groupID_) {
                groupID = groupID_;
            }

            @Override
            protected String doInBackground(Void... params) {
                String ret;
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet(String.format("%s%d", urlToGetLeagueByName, groupID));
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
                        rankingListElements = parseJSONArray(jsonArray);
                        rankingAdapter = new RankingAdapter(RankingActivity.this, 0, rankingListElements);
                        listView.setAdapter(rankingAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            private ArrayList<RankingListElement> parseJSONArray(JSONArray jsonArray) throws JSONException {
                int i = 0;
                ArrayList<RankingListElement> rankingListElements = new ArrayList<>();
                while (i < jsonArray.length()) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int teamID;
                    if (jsonObject.getJSONObject("pk").isNull("team")) {
                        teamID = -1;
                    } else {
                        teamID = jsonObject.getJSONObject("pk").getJSONObject("team").getInt("id");
                    }
                    String setsWon = jsonObject.getString("setsWon");
                    String setsLost = jsonObject.getString("setsLost");
                    String allScoresWon = jsonObject.getString("allScoresWon");
                    String allScoresLost = jsonObject.getString("allScoresLost");
                    String points = jsonObject.getString("points");
                    String team;
                    if (teamID == -1) {
                        team = "Nie";
                    } else {
                        team = "Tak";
                    }
                    rankingListElements.add(new RankingListElement(setsWon, setsLost, allScoresWon, allScoresLost, points, team, teamID));
                    i++;
                }
                return rankingListElements;
            }
        }
        new RankingExecute(groupId).execute();
    }
}

class RankingAdapter extends ArrayAdapter<RankingListElement> {
    private ArrayList<RankingListElement> listOfElements;
    private static LayoutInflater inflater = null;

    public RankingAdapter (Activity activity, int textViewResourceId, ArrayList<RankingListElement> element) {
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
        public TextView setsWon;
        public TextView setsLost;
        public TextView scoresWon;
        public TextView scoresLost;
        public TextView points;
        public TextView team;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.ranking_list_element, null);
                holder = new ViewHolder();
                holder.setsWon = (TextView) vi.findViewById(R.id.winSetLabelTV);
                holder.setsLost = (TextView) vi.findViewById(R.id.lostSetLabelTV);
                holder.scoresWon = (TextView) vi.findViewById(R.id.scoresWonLabelTV);
                holder.scoresLost = (TextView) vi.findViewById(R.id.scoresLostLabelTV);
                holder.points = (TextView) vi.findViewById(R.id.pointsLabelTV);
                holder.team = (TextView) vi.findViewById(R.id.teamLabelTV);
                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }
            holder.setsWon.setText(listOfElements.get(position).getSetsWon());
            holder.setsWon.setTag(listOfElements.get(position).getTeamID());
            holder.setsLost.setText(listOfElements.get(position).getSetsLost());
            holder.scoresWon.setText(listOfElements.get(position).getScoresWon());
            holder.scoresLost.setText(listOfElements.get(position).getScoresLost());
            holder.points.setText(listOfElements.get(position).getPoints());
            holder.team.setText(listOfElements.get(position).getTeam());
        } catch (Exception e) { }

        return vi;
    }
}

class RankingListElement {
    private String setsWon, setsLost, scoresWon, scoresLost, points, team;
    private int teamID;

    public RankingListElement(String setsWon_, String setsLost_, String scoresWon_, String scoresLost_, String points_, String team_, int teamID_) {
        setsWon = setsWon_;
        setsLost = setsLost_;
        scoresWon = scoresWon_;
        scoresLost = scoresLost_;
        points = points_;
        team = team_;
        teamID = teamID_;
    }
    public String getSetsWon() { return setsWon; }
    public String getSetsLost() { return setsLost; }
    public String getScoresWon() { return scoresWon; }
    public String getScoresLost() { return scoresLost; }
    public String getPoints() { return points; }
    public String getTeam() { return team; }
    public int getTeamID() { return teamID; }
}