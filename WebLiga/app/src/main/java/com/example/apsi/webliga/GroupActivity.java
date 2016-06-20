package com.example.apsi.webliga;

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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
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
    int groupId;

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
        groupId = intent.getIntExtra("ID", 0);
        new GroupExecute(groupId).execute();

        //wywolanie jsona do sprawdzenia czy dla istniejacego sedziego liga juz jest na jego liscie
        final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();
        if (globalActivity.getIsReferee() != null && globalActivity.getIsReferee().equals("Y")) {
            this.pobranieLigSedziego();
            Button dodajdoLigiSedzia = (Button) findViewById(R.id.dodajdoLigiSedzia);
            //przycisk od sedziego staje sie widoczny
            if (dodajdoLigiSedzia != null) {
                dodajdoLigiSedzia.setVisibility(View.VISIBLE);
            }
        }

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
                            int id = jsonObject.getInt("id");
                            leaguesReferee.add(id);
                            i++;
                            //sprawdza czy na liscie lig sedziego jest aktualnie otwarta liga
                            if (id == groupId) {
                                Button dodajdoLigiSedzia = (Button) findViewById(R.id.dodajdoLigiSedzia);
                                //przycisk od sedziego staje sie widoczny
                                if (dodajdoLigiSedzia != null) {
                                    dodajdoLigiSedzia.setVisibility(View.GONE);
                                }
                            }
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

    public void dodajdoLigiSedzia(final View view) {

        final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();
        final HttpContext localContext = globalActivity.getLocalContext();
        class signToLeagueReferee extends AsyncTask<Void, Void, String> {
            private int leagueID;
            private final String urlToGetLeagueByName =
                    "http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/referee/signToLeague";

            public signToLeagueReferee(int leagueID_) {
                leagueID = leagueID_;
            }

            @Override
            protected String doInBackground(Void... params) {
                String ret;
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpPost request = new HttpPost(urlToGetLeagueByName);

                    ArrayList<NameValuePair> pairs = new ArrayList<>();
                    pairs.add(new BasicNameValuePair("leagueId", Integer.toString(leagueID)));
                    request.setEntity(new UrlEncodedFormEntity(pairs));
                    HttpResponse response = client.execute(request,localContext);
                    BufferedReader rd = new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = rd.readLine()) != null) {result.append(line);
                    }
                    ret = result.toString();
                } catch (Exception e) {
                    return e.toString();
                }
                return ret;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result.equals("{\"description\":\"Pomyślnie zapisano wniosek o przypisanie sędziego do ligi. \",\"status\":\"OK\"}")){
                    Toast.makeText(getBaseContext(), "Dodano sędziego!", Toast.LENGTH_LONG).show();
                    Button dodajdoLigiSedzia = (Button) findViewById(R.id.dodajdoLigiSedzia);
                    dodajdoLigiSedzia.setVisibility(View.INVISIBLE);
                }
                else
                    Toast.makeText(getBaseContext(), "Nie udalo się dodać sędziego!", Toast.LENGTH_LONG).show();
            }
        }

        new signToLeagueReferee(groupId).execute();
    }
}

class GroupAdapter extends ArrayAdapter<GroupListElement> {
    private ArrayList<GroupListElement> listOfElements;
    private static LayoutInflater inflater = null;

    public GroupAdapter(Activity activity, int textViewResourceId, ArrayList<GroupListElement> element) {
        super(activity, textViewResourceId, element);
        try {
            this.listOfElements = element;
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return listOfElements.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

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
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    public String getMatches() {
        return matches;
    }

    public String getTeamPlayers() {
        return teamPlayers;
    }

    public String getPromotions() {
        return promotions;
    }

    public String getPhase() {
        return phase;
    }

    public int getGroupID() {
        return groupID;
    }
}