package com.example.apsi.webliga;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

public class CreateLeagueActivity extends AppCompatActivity {
    private String leagueName, sportName, sportType, sportDescription, leagueType, matchEnter, leagueDescription;
    private int maxMembers, season, year, matchesInGroup;

    public int getPointsForWin() {
        return pointsForWin;
    }

    public int getPointsForDraw() {
        return pointsForDraw;
    }

    public int getPointsForLose() {
        return pointsForLose;
    }

    public JSONObject getLeagueJSON() {
        return leagueJSON;
    }

    private int pointsForWin, pointsForDraw, pointsForLose;
    private JSONObject userJSON, leagueJSON;

    public int getMatchesInGroup() {
        return matchesInGroup;
    }

    public int getYear() {
        return year;
    }

    public int getSeason() {
        return season;
    }

    public String getLeagueDescription() {
        return leagueDescription;
    }

    public String getMatchEnter() {
        return matchEnter;
    }

    public String getLeagueType() {
        return leagueType;
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    public String getSportDescription() {
        return sportDescription;
    }

    public String getSportType() {
        return sportType;
    }

    public String getSportName() {
        return sportName;
    }

    public String getLeagueName() {
        return leagueName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_league);
    }

    public void backToMyLeaguesActivity(View view) {
        Intent intent = new Intent(CreateLeagueActivity.this, MyLeaguesActivity.class);
        startActivity(intent);
    }

    public void nextFormCreateLeagueActivity(View view) {
        boolean isFormFilledOK = false;
        if (saveLeagueName() && saveSportName() && saveMaxMembers())
            isFormFilledOK = true;
        saveSportType();
        saveSportDescription();
        if (isFormFilledOK) {
            setContentView(R.layout.activity_create_league2);
            if (leagueType != null && matchEnter != null)
                fillForm2WithPreviousValues();
        }
    }

    private void saveSportDescription(){
        EditText editText = (EditText) findViewById(R.id.sportDescriptionET);
        sportDescription = editText.getText().toString();
    }

    private boolean saveLeagueName() {
        EditText editText = (EditText) findViewById(R.id.enterNeLeagueNameET);
        leagueName = editText.getText().toString();
        if (leagueName == null || leagueName.equals("")) {
            showToast("Nazwa ligi nie może być pusta");
            return false;
        }
        return true;
    }

    private boolean saveSportName() {
        EditText editText = (EditText) findViewById(R.id.enterNeLeagueSportNameET);
        sportName = editText.getText().toString();
        if (sportName == null || sportName.equals("")){
            showToast("Nazwa sportu nie może być pusta");
            return false;
        }
        return true;
    }

    private void saveSportType() {
        RadioButton radioButton = (RadioButton) findViewById(R.id.indSportRB);
        if (radioButton.isChecked())
            sportType = "ind";
        else
            sportType = "team";
    }

    private boolean saveMaxMembers() {
        EditText editText = (EditText) findViewById(R.id.maxMemberCountET);
        maxMembers = -1;
        if (editText.getText().length() != 0)
            maxMembers = Integer.parseInt(editText.getText().toString());
        if (maxMembers == -1) {
            showToast("Liczba uczestników musi być zdefiniowana");
            return false;
        }
        return true;
    }

    private void showToast(String message) {
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
    }

    public void backFormCreateLeagueActivity2(View view) {
        setContentView(R.layout.activity_create_league);
        fillForm1WithPreviousValues();
    }

    private void fillForm1WithPreviousValues() {
        EditText editText = (EditText) findViewById(R.id.enterNeLeagueNameET);
        editText.setText(leagueName);
        editText = (EditText) findViewById(R.id.enterNeLeagueSportNameET);
        editText.setText(sportName);
        editText = (EditText) findViewById(R.id.maxMemberCountET);
        editText.setText(String.valueOf(maxMembers));
        editText = (EditText) findViewById(R.id.sportDescriptionET);
        editText.setText(sportDescription);
        if (sportType.equals("ind")){
            RadioButton radioButton = (RadioButton) findViewById(R.id.indSportRB);
            radioButton.setChecked(true);
        } else if (sportType.equals("team")) {
            RadioButton radioButton = (RadioButton) findViewById(R.id.teamSportRB);
            radioButton.setChecked(true);
        }
    }

    public void nextFormCreateLeagueActivity2(View view) {
        saveLeagueType();
        saveMatchEnter();
        saveLeagueDescription();
        saveMatchesInGroup();
        boolean isFormFilledOk = false;
        if (saveSeason() && saveYear())
            isFormFilledOk = true;
        if (isFormFilledOk)
            setContentView(R.layout.activity_create_league3);
            fillForm3WithPreviousValues();
    }

    private boolean saveSeason() {
        EditText editText = (EditText) findViewById(R.id.enterNewLeagueSeasonET);
        season = -1;
        if (editText.getText().length() != 0)
            season = Integer.parseInt(editText.getText().toString());
        if (season == -1) {
            showToast("Sezon musi być zdefiniowany");
            return false;
        }
        return true;
    }

    private boolean saveYear() {
        EditText editText = (EditText) findViewById(R.id.enterNeLeagueYearET);
        year = -1;
        if (editText.getText().length() != 0)
            year = Integer.parseInt(editText.getText().toString());
        if (year == -1) {
            showToast("Rok musi być zdefiniowany");
            return false;
        }
        return true;
    }

    private void saveLeagueType() {
        RadioButton allRButton = (RadioButton) findViewById(R.id.allTypeRB);
        RadioButton tournamentRButton = (RadioButton) findViewById(R.id.tournamentTypeRB);
        if (allRButton.isChecked())
            leagueType = "group";
        else if (tournamentRButton.isChecked())
            leagueType = "knock-out";
        else
            leagueType = "groupKO";
    }

    private void saveMatchEnter() {
        RadioButton freeRB = (RadioButton) findViewById(R.id.freeRB);
        if (freeRB.isChecked())
            matchEnter = "free";
        else
            matchEnter = "ticket";
    }

    private void saveLeagueDescription() {
        EditText editText = (EditText) findViewById(R.id.leagueDescriptionET);
        leagueDescription = editText.getText().toString();
    }

    private void saveMatchesInGroup() {
        EditText editText = (EditText) findViewById(R.id.matchesInGroupCountET);
        matchesInGroup = Integer.parseInt(editText.getText().toString());
    }

    public void backFormCreateLeagueActivity3(View view) {
        setContentView(R.layout.activity_create_league2);
        fillForm2WithPreviousValues();
    }

    private boolean savePointsForWin() {
        EditText editText = (EditText) findViewById(R.id.pointForWinET);
        pointsForWin = -1;
        if (editText.getText().length() != 0)
            pointsForWin = Integer.parseInt(editText.getText().toString());
        if (pointsForWin == -1) {
            showToast("Punkty za wygraną muszą być zdefiniowane");
            return false;
        }
        return true;
    }

    private boolean savePointsForDraw() {
        EditText editText = (EditText) findViewById(R.id.pointForDrawET);
        pointsForDraw = -1;
        if (editText.getText().length() != 0)
            pointsForDraw = Integer.parseInt(editText.getText().toString());
        if (pointsForDraw == -1) {
            showToast("Punkty za wygraną muszą być zdefiniowane");
            return false;
        }
        return true;
    }

    private boolean savePointsForLose() {
        EditText editText = (EditText) findViewById(R.id.pointForLoseET);
        pointsForLose = -1;
        if (editText.getText().length() != 0)
            pointsForLose = Integer.parseInt(editText.getText().toString());
        if (pointsForLose == -1) {
            showToast("Punkty za wygraną muszą być zdefiniowane");
            return false;
        }
        return true;
    }

    public void createNewLeague(View view) {
//        savePointsForWin();
//        savePointsForDraw();
//        savePointsForLose();
//        getUserJSON();
    }

    private void fillForm2WithPreviousValues() {
        EditText editText = (EditText) findViewById(R.id.enterNewLeagueSeasonET);
        editText.setText(String.valueOf(season));
        editText = (EditText) findViewById(R.id.enterNeLeagueYearET);
        editText.setText(String.valueOf(year));
        editText = (EditText) findViewById(R.id.matchesInGroupCountET);
        editText.setText(String.valueOf(matchesInGroup));
        editText = (EditText) findViewById(R.id.leagueDescriptionET);
        editText.setText(leagueDescription);
        if (leagueType.equals("group")){
            RadioButton radioButton = (RadioButton) findViewById(R.id.allTypeRB);
            radioButton.setChecked(true);
        } else if (leagueType.equals("groupKO")) {
            RadioButton radioButton = (RadioButton) findViewById(R.id.tournamentWithGroupTypeRB);
            radioButton.setChecked(true);
        } else if (leagueType.equals("knock-out")) {
            RadioButton radioButton = (RadioButton) findViewById(R.id.tournamentTypeRB);
            radioButton.setChecked(true);
        }
        if (matchEnter.equals("free")){
            RadioButton radioButton = (RadioButton) findViewById(R.id.freeRB);
            radioButton.setChecked(true);
        } else if (matchEnter.equals("ticket")){
            RadioButton radioButton = (RadioButton) findViewById(R.id.nonfreeRB);
            radioButton.setChecked(true);
        }
    }

    private void fillForm3WithPreviousValues() {
        EditText editText = (EditText) findViewById(R.id.pointForWinET);
        editText.setText(String.valueOf(pointsForWin));
        editText = (EditText) findViewById(R.id.pointForDrawET);
        editText.setText(String.valueOf(pointsForDraw));
        editText = (EditText) findViewById(R.id.pointForLoseET);
        editText.setText(String.valueOf(pointsForLose));
    }

    private void getUserJSON() {
        class GetUserOrgExecute extends AsyncTask<Void, Void, String> {
            private String organizerId;
            private final String urlToGetOrganizerById =
                    "http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/organizer/getOrganizerByUserId?id=";

            public GetUserOrgExecute(String organizerId_) {
                organizerId = organizerId_;
            }

            @Override
            protected String doInBackground(Void... params) {
                String ret;
                final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet(urlToGetOrganizerById + organizerId);
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
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    try {
                        userJSON = new JSONObject(result);
                        leagueJSON = new JSONObject();
                        try {
                            leagueJSON.put("id", null);
                            JSONObject organizer = new JSONObject();
                            organizer.put("id", null);
                            organizer.put("user", userJSON);
                            leagueJSON.put("organizer", organizer);
                            JSONObject sportJSON = new JSONObject();
                            sportJSON.put("id", null);
                            sportJSON.put("name", getSportName());
                            sportJSON.put("type", getSportType());
                            sportJSON.put("playersNumber", getMaxMembers());
                            sportJSON.put("description", getSportDescription());
                            sportJSON.put("referees", new JSONArray());
                            sportJSON.put("players", new JSONArray());
                            leagueJSON.put("sport", sportJSON);
                            leagueJSON.put("name", getLeagueName());
                            leagueJSON.put("season", getSeason());
                            leagueJSON.put("year", getYear());
                            leagueJSON.put("active", "active");
                            leagueJSON.put("type", getLeagueType());
                            leagueJSON.put("description", getLeagueDescription());
                            leagueJSON.put("matchesInGroupNumber", getMatchesInGroup());
                            leagueJSON.put("pointsTable", new JSONArray());
                            //TODO: poprawic bo jest na sztywno
                            JSONObject scoringRulesJSON = new JSONObject();
                            scoringRulesJSON.put("id", null);
                            scoringRulesJSON.put( "scoringType", "victory");
                            scoringRulesJSON.put( "threshold", null);
                            scoringRulesJSON.put( "pointsAboveWin", null);
                            scoringRulesJSON.put( "pointsAboveLost", null);
                            scoringRulesJSON.put( "pointsBelowWin", null);
                            scoringRulesJSON.put( "pointsBelowLost", null);
                            scoringRulesJSON.put( "pointsEqualWin", null);
                            scoringRulesJSON.put( "pointsEqualLost", null);
                            scoringRulesJSON.put( "pointsLost", getPointsForLose());
                            scoringRulesJSON.put( "pointsVictory", getPointsForWin());
                            scoringRulesJSON.put( "pointsDraw", getPointsForDraw());
                            leagueJSON.put("scoringRules", scoringRulesJSON);
                            createLeaguePost();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        int  i = 0;
                        i++;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();
        try {
            String str_result= new GetUserOrgExecute(String.valueOf(globalActivity.getUserID())).execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void createLeaguePost() {
        class CreateLeagueExecute extends AsyncTask<Void, Void, String> {
            private JSONObject league;
            private final String urlToCreateLeague =
                    "http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/organizer/createLeague";

            public CreateLeagueExecute(JSONObject league_) {
                league = league_;
            }

            @Override
            protected String doInBackground(Void... params) {
                String ret;
                final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpPost request = new HttpPost(urlToCreateLeague);
                    String jsonObjectAsString = league.toString();
                    StringEntity stringEntity = new StringEntity(jsonObjectAsString);
                    request.setEntity(stringEntity);
                    request.setHeader("Accept", "application/json");
                    request.setHeader("Content-type", "application/json");
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
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    try {
                        JSONObject response;
                        response = new JSONObject(result);
                        int i = 0;
                        i++;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        try {
            String result = new CreateLeagueExecute(leagueJSON).execute(). get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void switchLayoutToPointsForMatchOption(View view) {

    }

    public void switchLayoutToPointsForPlaceOption(View view) {

    }

    public void switchLayoutToPointsForMatchStandardOption(View view) {

    }
}



