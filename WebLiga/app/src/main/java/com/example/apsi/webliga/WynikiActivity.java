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

public class WynikiActivity extends AppCompatActivity {
    ListView listView;
    WynikiAdapter wynikiAdapter;
    private ArrayList<WynikiListElement> wynikiListElements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminarz);
        listView = (ListView) findViewById(R.id.listView2);

        class GroupExecute extends AsyncTask<Void, Void, String> {
            private int groupID;
            private final String urlToGetLeagueByName =
                    "http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/home/getMatchesInGroup?id=";

            public GroupExecute(int groupID_) {
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
            protected void onPostExecute(String result){
                JSONArray jsonArray;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    try {
                        jsonArray = new JSONArray(result);
                        wynikiListElements = parseJSONArray(jsonArray);
                        wynikiAdapter = new WynikiAdapter(WynikiActivity.this, 0, wynikiListElements);
                        listView.setAdapter(wynikiAdapter);
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
                    if(jsonObject.getJSONObject("league").getJSONObject("sport").getString("type").equals("team"))
                    {
                        String date = jsonObject.getString("date");
                        JSONArray array = jsonObject.getJSONArray("scoreTeam");
                        if(array.length() > 0)
                        {
                            JSONObject teamObject1 = array.getJSONObject(0);
                            String score1 = teamObject1.getString("score");
                            String team1 = teamObject1.getJSONObject("pk").getJSONObject("team").getString("name");
                            JSONObject teamObject2 = array.getJSONObject(1);
                            String score2 = teamObject2.getString("score");
                            String team2 = teamObject2.getJSONObject("pk").getJSONObject("team").getString("name");
                            groupListElements.add(new WynikiListElement(id, date, team1, score1, team2, score2));
                        }
                    } else {
                        String date = jsonObject.getString("date");
                        JSONArray array = jsonObject.getJSONArray("scoreInd");
                        if(array.length() > 0)
                        {
                            JSONObject teamObject1 = array.getJSONObject(0);
                            String score1 = teamObject1.getString("score");
                            String team1 = teamObject1.getJSONObject("pk").getJSONObject("player").getJSONObject("user").getString("name") + " " + teamObject1.getJSONObject("pk").getJSONObject("player").getJSONObject("user").getString("surname");
                            JSONObject teamObject2 = array.getJSONObject(1);
                            String score2 = teamObject2.getString("score");
                            String team2 = teamObject2.getJSONObject("pk").getJSONObject("player").getJSONObject("user").getString("name") + " " + teamObject2.getJSONObject("pk").getJSONObject("player").getJSONObject("user").getString("surname");
                            groupListElements.add(new WynikiListElement(id, date, team1, score1, team2, score2));
                        }
                    }
                    i++;
                }
                return groupListElements;
            }
        }
        Intent intent = getIntent();
        int groupId = intent.getIntExtra("ID", 0);
        new GroupExecute(groupId).execute();
    }
}

class WynikiAdapter extends ArrayAdapter<WynikiListElement> {
    private ArrayList<WynikiListElement> listOfElements;
    private static LayoutInflater inflater = null;

    public WynikiAdapter (Activity activity, int textViewResourceId, ArrayList<WynikiListElement> element) {
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
        public TextView date;
        public TextView team1;
        public TextView score1;
        public TextView team2;
        public TextView score2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.wyniki_list_element, null);
                holder = new ViewHolder();
                holder.date = (TextView) vi.findViewById(R.id.dateLabelTV);
                holder.team1 = (TextView) vi.findViewById(R.id.team1LabelTV);
                holder.score1 = (TextView) vi.findViewById(R.id.team1ScoreLabelTV);
                holder.team2 = (TextView) vi.findViewById(R.id.team2LabelTV);
                holder.score2 = (TextView) vi.findViewById(R.id.team2ScoreLabelTV);
                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }
            holder.date.setText(listOfElements.get(position).getDate());
            holder.team1.setText(listOfElements.get(position).getTeam1());
            holder.score1.setText(listOfElements.get(position).getScore1());
            holder.team2.setText(listOfElements.get(position).getTeam2());
            holder.score2.setText(listOfElements.get(position).getScore2());
        } catch (Exception e) { }

        return vi;
    }
}

class WynikiListElement {
    private String date, team1, score1, team2, score2;
    int id;

    public WynikiListElement(int id_, String date_, String team1_, String score1_, String team2_, String score2_) {
        id = id_;
        date = date_;
        team1 = team1_;
        score1 = score1_;
        team2 = team2_;
        score2 = score2_;
    }
    public String getDate() { return date; }
    public String getTeam1() { return team1; }
    public String getScore1() { return score1; }
    public String getTeam2() { return team2; }
    public String getScore2() { return score2; }
    public int getId() { return id; }
}