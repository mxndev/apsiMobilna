package com.example.apsi.webliga;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LeagueDetailsActivity extends AppCompatActivity {

    ArrayList<LeagueMatchListElement> nextMatches;
    ArrayList<LeagueMatchListElement> previousMatches;
    ArrayList<TeamListElement> teamList;
    ListView previousMatchesLV;
    ListView nextMatchesLV;
    final String INDIVIDUAL_SPORT = "ind";
    final String TEAM_SPORT = "team";
    int leagueId, teamId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_league_details);
        initLeagueInfo();
    }

    private void initLeagueInfo() {
        String temp;
        Intent intent = getIntent();
        temp = intent.getStringExtra("sport");
        TextView textView = (TextView) findViewById(R.id.disciplineTV);
        textView.setText(temp);
        temp = intent.getStringExtra("organizer");
        textView = (TextView) findViewById(R.id.organizerTV);
        textView.setText(temp);
        temp = intent.getStringExtra("name");
        final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();
        globalActivity.setLeagueName(temp);
        textView = (TextView) findViewById(R.id.leagueNameTV);
        textView.setText(temp);
        leagueId = intent.getIntExtra("ID", 0);
        Button signIndToLeagueBtn = (Button) findViewById(R.id.signIndToLeagueBtn);
        Button signTeamToLeagueBtn = (Button) findViewById(R.id.signTeamToLeagueBtn);

        if (globalActivity.getLocalContext() != null)
            if (intent.getStringExtra("sportsType").equals(INDIVIDUAL_SPORT))
                signIndToLeagueBtn.setVisibility(Button.VISIBLE);
            else if (intent.getStringExtra("sportsType").equals(TEAM_SPORT))
                signTeamToLeagueBtn.setVisibility(Button.VISIBLE);
        new LeagueDetailsExecute(leagueId).execute();
    }

    public void showLeagueRanking(View view) {
        Intent intent = getIntent();
        int temp = intent.getIntExtra("ID", 0);
        intent = new Intent(LeagueDetailsActivity.this, GroupActivity.class);
        intent.putExtra("ID", temp);
        startActivity(intent);
    }

    public void signIndToLeague(View view) {
        class SignIndToLeagueExecute extends AsyncTask<Void, Void, String> {
            private int leagueId;
            private final String urlToSignIndToLeague =
                    "http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/player/signPlayerForLeague?id=";

            public SignIndToLeagueExecute(int leagueId) {
                this.leagueId = leagueId;
            }

            @Override
            protected String doInBackground(Void... params) {
                String ret;
                try {
                    final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();
                    final HttpContext localContext = globalActivity.getLocalContext();
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet(String.format("%s%d", urlToSignIndToLeague, leagueId));
                    HttpResponse response = client.execute(request,localContext);
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
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(result);
                    Toast.makeText(getBaseContext(), jsonObject.getString("description"), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        new SignIndToLeagueExecute(leagueId).execute();
    }

    class ChooseTeamDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final TeamListAdapter teamListAdapter = new TeamListAdapter(LeagueDetailsActivity.this, 0, teamList);
            builder.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            builder.setAdapter(teamListAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    TeamListElement tle = teamListAdapter.getItem(which);
                    teamId = tle.getTeamId();
                    new SignTeamToLeagueExecute(leagueId, teamId).execute();
                }
            });
            return builder.create();
        }
    }

    class SignTeamToLeagueExecute extends AsyncTask<Void, Void, String> {
        private int leagueId;
        private int teamId;
        private final String urlToSignTeamToLeague =
                "http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/player/signTeamToLeague";

        public SignTeamToLeagueExecute(int leagueId, int teamId) {
            this.leagueId = leagueId;
            this.teamId = teamId;
        }

        @Override
        protected String doInBackground(Void... params) {
            String ret;
            try {
                final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();
                final HttpContext localContext = globalActivity.getLocalContext();
                HttpClient client = new DefaultHttpClient();
                String requestString = urlToSignTeamToLeague + "?team_id=" + String.valueOf(teamId) +
                        "&league_id=" + String.valueOf(leagueId);
                HttpPost request = new HttpPost(requestString);
                request.setHeader("Accept", "application/json");
                request.setHeader("Content-type", "application/json");
                HttpResponse response = client.execute(request,localContext);
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
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(result);
                Toast.makeText(getBaseContext(), jsonObject.getString("description"), Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void signTeamToLeagueBtn(View view) {
        final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();

        teamList = new ArrayList<>();
        class GetUserExecute extends AsyncTask<Void, Void, String> {
            private final String urlToGetUserTeams =
                    "http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/player/getTeams";
            @Override
            protected String doInBackground(Void... params) {
                String ret;
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet(urlToGetUserTeams);
                    HttpResponse response = client.execute(request, globalActivity.getLocalContext());
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
                        while (i<jsonArray.length()){
                            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                            int teamId = jsonObject.getInt("id");
                            String teamName = jsonObject.getString("name");
                            teamList.add(new TeamListElement(teamId, teamName));
                            i++;
                        }
                        FragmentManager fm = getFragmentManager();
                        ChooseTeamDialogFragment ctdf = new ChooseTeamDialogFragment();
                        ctdf.show(fm, "tag");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        new GetUserExecute().execute();
}

    class TeamListElement {
        private int teamId;
        private String teamName;
        TeamListElement(int teamId, String teamName) {
            this.teamId = teamId;
            this.teamName = teamName;
        }
        public int getTeamId() { return teamId;  }
        public String getTeamName() { return teamName; }
    }

    class LeagueDetailsExecute extends AsyncTask<Void, Void, String> {
        private int leagueID;
        private final String urlToGetLeagueMatches =
                "http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/home/getAllMatchesByLeague?id=";

        public LeagueDetailsExecute(int leagueID_) {
            leagueID = leagueID_;
        }

        @Override
        protected String doInBackground(Void... params) {
            String ret;
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet(String.format("%s%d", urlToGetLeagueMatches, leagueID));
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
            String matchResult, place;
            Date date;
            try {
                jsonArray = new JSONArray(result);
                int i = 0;
                if (previousMatches == null)
                    previousMatches = new ArrayList<>();
                if (nextMatches == null)
                    nextMatches = new ArrayList<>();
                previousMatches.clear();
                nextMatches.clear();
                while (i < jsonArray.length()) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String dateAsString = jsonObject.getString("date");
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    date = dateFormat.parse(dateAsString);
                    Date currentDate = new Date();
                    dateFormat.format(currentDate);
                    boolean isTeamSport = true;
                    if (jsonObject.getJSONArray("scoreInd").length() > 0)
                        isTeamSport = false;
                    if (isTeamSport){
                        JSONObject team = (JSONObject) jsonObject.getJSONArray("scoreTeam").get(0);
                        matchResult = team.getJSONObject("pk").getJSONObject("team").getString("name");
                        matchResult += " - " + team.getString("score") + " ";
                        team = (JSONObject) jsonObject.getJSONArray("scoreTeam").get(1);
                        matchResult += team.getJSONObject("pk").getJSONObject("team").getString("name");
                        matchResult += " - " + team.getString("score");
                        place = jsonObject.getJSONObject("place").getString("address") + ", " +
                                jsonObject.getJSONObject("place").getString("postalCode") + " " +
                                jsonObject.getJSONObject("place").getString("city");
                    } else {
                        JSONObject ind = (JSONObject) jsonObject.getJSONArray("scoreInd").get(0);
                        matchResult = ind.getJSONObject("pk").getJSONObject("player").getJSONObject("user").
                                getString("name");
                        matchResult += " - " + ind.getString("score") + " ";
                        ind = (JSONObject) jsonObject.getJSONArray("scoreInd").get(1);
                        matchResult += ind.getJSONObject("pk").getJSONObject("player").getJSONObject("user").
                                getString("name");
                        matchResult += " - " + ind.getString("score");
                        place = jsonObject.getJSONObject("place").getString("address") + ", " +
                                jsonObject.getJSONObject("place").getString("postalCode") + " " +
                                jsonObject.getJSONObject("place").getString("city");
                    }
                    if (date.before(currentDate)) {
                        previousMatches.add(new LeagueMatchListElement(matchResult, place, dateFormat.format(date).toString()));
                    } else {
                        nextMatches.add(new LeagueMatchListElement(matchResult, place, dateFormat.format(date).toString()));
                    }
                    i++;
                }
                if (previousMatches != null){
                    previousMatchesLV = (ListView) findViewById(R.id.previousLeagueMatchesList);
                    previousMatchesLV.setAdapter(new LeaguePreviousMatchesAdapter(LeagueDetailsActivity.this, 0, previousMatches));
                }
                if (nextMatches != null){
                    nextMatchesLV = (ListView) findViewById(R.id.leagueMatchesList);
                    nextMatchesLV.setAdapter(new LeagueNextMatchesAdapter(LeagueDetailsActivity.this, 0, nextMatches));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}

class LeagueMatchListElement {
    private String date, place, result;

    public LeagueMatchListElement(String result, String place, String date) {
        this.result = result;
        this.place = place;
        this.date = date;
    }

    public String getPlace() { return place; }
    public String getDate() { return date; }
    public String getResult() { return result; }
}

class LeaguePreviousMatchesAdapter extends ArrayAdapter<LeagueMatchListElement> {
    private ArrayList<LeagueMatchListElement> listOfElements;
    private static LayoutInflater inflater = null;

    public LeaguePreviousMatchesAdapter (Activity activity, int textViewResourceId,ArrayList<LeagueMatchListElement> element) {
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
        public TextView matchDate;
        public TextView matchResult;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.league_previous_match_list_element, null);
                holder = new ViewHolder();
                holder.matchDate = (TextView) vi.findViewById(R.id.dateMP);
                holder.matchResult = (TextView) vi.findViewById(R.id.resultMP);
                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }
            holder.matchDate.setText(listOfElements.get(position).getDate());
            holder.matchResult.setText(listOfElements.get(position).getResult());
        } catch (Exception e) { }

        return vi;
    }
}

class LeagueNextMatchesAdapter extends ArrayAdapter<LeagueMatchListElement> {
    private ArrayList<LeagueMatchListElement> listOfElements;
    private static LayoutInflater inflater = null;

    public LeagueNextMatchesAdapter (Activity activity, int textViewResourceId,ArrayList<LeagueMatchListElement> element) {
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
        public TextView matchDate;
        public TextView matchPlace;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.league_next_match_list_element, null);
                holder = new ViewHolder();
                holder.matchDate = (TextView) vi.findViewById(R.id.dateMN);
                holder.matchPlace = (TextView) vi.findViewById(R.id.placeMN);
                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }
            holder.matchDate.setText(listOfElements.get(position).getDate());
            holder.matchPlace.setText(listOfElements.get(position).getPlace());
        } catch (Exception e) { }

        return vi;
    }
}

class TeamListAdapter extends ArrayAdapter<LeagueDetailsActivity.TeamListElement> {
    private ArrayList<LeagueDetailsActivity.TeamListElement> listOfElements;
    private static LayoutInflater inflater = null;

    public TeamListAdapter (Activity activity, int textViewResourceId,ArrayList<LeagueDetailsActivity.TeamListElement> element) {
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
        public TextView teamName;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.user_teams_list_element, null);
                holder = new ViewHolder();
                holder.teamName = (TextView) vi.findViewById(R.id.userTeamNameTV);
                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }
            holder.teamName.setText(listOfElements.get(position).getTeamName());
        } catch (Exception e) { }

        return vi;
    }
}

