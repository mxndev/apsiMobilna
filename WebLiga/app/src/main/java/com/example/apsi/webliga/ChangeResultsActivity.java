package com.example.apsi.webliga;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ChangeResultsActivity extends AppCompatActivity {

    Toolbar toolbar;
    MatchInfo matchInfo;
    ArrayList<PartOfGame> gameDetailsFirst;
    ArrayList<PartOfGame> gameDetailsSecond;
    ArrayList<PartOfGameListElement> partOfGameListElements;
    TextView firstNameTV, secondNameTV;
    EditText firstScoreET, secondScoreET;
    ListView listView;
    ChangeResultsAdapter changeResultsAdapter;
    JSONObject jsonObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_results);
        initToolBar();
        final Spinner spinner = (Spinner) findViewById(R.id.spinnerToolbar2);
        firstNameTV = (TextView) findViewById(R.id.firstNameTV);
        secondNameTV = (TextView) findViewById(R.id.secondNameTV);
        firstScoreET = (EditText) findViewById(R.id.firstScore);
        secondScoreET = (EditText) findViewById(R.id.secondScore);
        listView = (ListView) findViewById(R.id.partOfGameList);
        getMatch();
        ArrayList<String> arrayList1 = new ArrayList<>();
        arrayList1.add("Menu");
        arrayList1.add("Menu");
        arrayList1.add("Wyszukaj ligę");
        arrayList1.add("Wyszukaj zespół");
        arrayList1.add("Moje mecze");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, arrayList1) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v;
                if (position == 0) {
                    TextView tv = new TextView(getContext());
                    tv.setHeight(0);
                    tv.setVisibility(View.GONE);
                    v = tv;
                } else {
                    v = super.getDropDownView(position, null, parent);
                }
                parent.setVerticalScrollBarEnabled(false);
                return v;
            }
        };
        adapter.setDropDownViewResource(R.layout.spinner_item);
        assert spinner != null;
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
                        startActivity(new Intent(ChangeResultsActivity.this, AfterLoginActivity.class));
                        spinner.setSelection(0);
                        break;
                    case 2:
                        startActivity(new Intent(ChangeResultsActivity.this, SearchLeagueActivity.class));
                        spinner.setSelection(0);
                        break;
                    case 3:
                        startActivity(new Intent(ChangeResultsActivity.this, SearchTeamActivity.class));
                        spinner.setSelection(0);
                        break;
                    case 4:
                        startActivity(new Intent(ChangeResultsActivity.this, MojeMeczeActivity.class));
                        spinner.setSelection(0);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) { }
        });
    }
    public void initToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.toolbarTitle);
        }
    }
    private void getMatch(){
        Intent intent = getIntent();
        int id  = intent.getIntExtra("ID", 0);
        new GetResultsExecute(id).execute();
    }
    public void addSet(View view) {
        PartOfGameListElement newSet = new PartOfGameListElement(firstNameTV.getText().toString(),
                secondNameTV.getText().toString(),0,0, partOfGameListElements.size() + 1);
        partOfGameListElements.add(newSet);
        changeResultsAdapter.notifyDataSetChanged();
    }

    public void confirmChanges(View view) throws JSONException {
        try {
            updateJSONObject(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new ChangeResultsExecute().execute();
    }

    private void updateJSONObject(JSONObject jsonObject) throws JSONException {
        if (isTeam(jsonObject)) {
            updateTeamsFinalScores(jsonObject);
            HashMap<Integer, Integer> setScoreMapFirst = new HashMap<>();
            HashMap<Integer, Integer> setScoreMapSecond = new HashMap<>();
            for (PartOfGameListElement listElement : partOfGameListElements) {
                setScoreMapFirst.put(listElement.setNumber, listElement.getFirstTeamScore());
                setScoreMapSecond.put(listElement.setNumber, listElement.getSecondTeamScore());
            }
            modifyFirstTeamsSets(jsonObject, setScoreMapFirst);
            modifySecondTeamsSets(jsonObject, setScoreMapSecond);

            for (int i = getCountOfSets(jsonObject); i < partOfGameListElements.size(); i++) {
                addFirstTeamsNewSets(jsonObject, setScoreMapFirst, i);
                addSecondTeamsNewSets(jsonObject, setScoreMapSecond, i);
            }
        } else {
            updatePlayersFinalScores(jsonObject);
            HashMap<Integer, Integer> setScoreMapFirst = new HashMap<>();
            HashMap<Integer, Integer> setScoreMapSecond = new HashMap<>();
            for (PartOfGameListElement listElement : partOfGameListElements) {
                setScoreMapFirst.put(listElement.setNumber, listElement.getFirstTeamScore());
                setScoreMapSecond.put(listElement.setNumber, listElement.getSecondTeamScore());
            }
            modifyFirstPlayersSets(jsonObject, setScoreMapFirst);
            modifySecondPlayersSets(jsonObject, setScoreMapSecond);
            for (int i = getPlayersCountOfSets(jsonObject); i < partOfGameListElements.size(); i++) {
                addFirstPlayersNewSets(jsonObject, setScoreMapFirst, i);
                addSecondPlayersNewSets(jsonObject, setScoreMapSecond, i);
            }
        }
    }

    private void addSecondPlayersNewSets(JSONObject jsonObject, HashMap<Integer, Integer> setScoreMapSecond, int i) throws JSONException {
        JSONObject secondPlayer = jsonObject.getJSONArray("scoreInd").getJSONObject(1).
                getJSONObject("pk").getJSONObject("player");
        JSONObject newSetSecond = new JSONObject();
        newSetSecond.put("id", null);
        newSetSecond.put("score", setScoreMapSecond.get(i + 1));
        newSetSecond.put("setNumber", i + 1);
        newSetSecond.put("player", secondPlayer);
        jsonObject.getJSONArray("scoreInd").getJSONObject(1).
                getJSONArray("scoreDetails").put(newSetSecond);
    }

    private void addFirstPlayersNewSets(JSONObject jsonObject, HashMap<Integer, Integer> setScoreMapFirst, int i) throws JSONException {
        JSONObject firstPlayer = jsonObject.getJSONArray("scoreInd").getJSONObject(0).
                getJSONObject("pk").getJSONObject("player");
        JSONObject newSetFirst = new JSONObject();
        newSetFirst.put("id", null);
        newSetFirst.put("score", setScoreMapFirst.get(i + 1));
        newSetFirst.put("setNumber", i + 1);
        newSetFirst.put("player", firstPlayer);
        jsonObject.getJSONArray("scoreInd").getJSONObject(0).
                getJSONArray("scoreDetails").put(newSetFirst);
    }

    private void modifySecondPlayersSets(JSONObject jsonObject, HashMap<Integer, Integer> setScoreMapSecond) throws JSONException {
        JSONArray secondPlayersSets = jsonObject.getJSONArray("scoreInd").getJSONObject(1).
                getJSONArray("scoreDetails");
        for (int i = 0; i < secondPlayersSets.length(); i++) {
            int setNumber = secondPlayersSets.getJSONObject(i).getInt("setNumber");
            jsonObject.getJSONArray("scoreInd").getJSONObject(1).
                    getJSONArray("scoreDetails").getJSONObject(i).put("score",
                    setScoreMapSecond.get(setNumber));
        }
    }

    private void modifyFirstPlayersSets(JSONObject jsonObject, HashMap<Integer, Integer> setScoreMapFirst) throws JSONException {
        JSONArray firstPlayersSets = jsonObject.getJSONArray("scoreInd").getJSONObject(0).
                getJSONArray("scoreDetails");
        for (int i = 0; i < firstPlayersSets.length(); i++) {
            int setNumber = firstPlayersSets.getJSONObject(i).getInt("setNumber");
            jsonObject.getJSONArray("scoreInd").getJSONObject(0).
                    getJSONArray("scoreDetails").getJSONObject(i).put("score",
                    setScoreMapFirst.get(setNumber));
        }
    }

    private int getPlayersCountOfSets(JSONObject jsonObject) throws JSONException {
        int c = jsonObject.getJSONArray("scoreInd").getJSONObject(0).
                getJSONArray("scoreDetails").length();
        return c;
    }

    private void updatePlayersFinalScores(JSONObject jsonObject) throws JSONException {
        jsonObject.getJSONArray("scoreInd").getJSONObject(0).
                put("score", Integer.parseInt(firstScoreET.getText().toString()));
        jsonObject.getJSONArray("scoreInd").getJSONObject(1).
                put("score", Integer.parseInt(secondScoreET.getText().toString()));
    }

    private void addSecondTeamsNewSets(JSONObject jsonObject, HashMap<Integer, Integer> setScoreMapSecond, int i) throws JSONException {
        JSONObject secondTeam = jsonObject.getJSONArray("scoreTeam").getJSONObject(1).
                getJSONObject("pk").getJSONObject("team");
        JSONObject newSetSecond = new JSONObject();
        newSetSecond.put("id", null);
        newSetSecond.put("score", setScoreMapSecond.get(i + 1));
        newSetSecond.put("setNumber", i + 1);
        newSetSecond.put("team", secondTeam);
        jsonObject.getJSONArray("scoreTeam").getJSONObject(1).
                getJSONArray("scoreDetails").put(newSetSecond);
    }

    private void addFirstTeamsNewSets(JSONObject jsonObject, HashMap<Integer, Integer> setScoreMapFirst, int i) throws JSONException {
        JSONObject firstTeam = jsonObject.getJSONArray("scoreTeam").getJSONObject(0).
                getJSONObject("pk").getJSONObject("team");
        JSONObject newSetFirst = new JSONObject();
        newSetFirst.put("id", null);
        newSetFirst.put("score", setScoreMapFirst.get(i + 1));
        newSetFirst.put("setNumber", i + 1);
        newSetFirst.put("team", firstTeam);
        jsonObject.getJSONArray("scoreTeam").getJSONObject(0).
                getJSONArray("scoreDetails").put(newSetFirst);
    }

    private void modifyFirstTeamsSets(JSONObject jsonObject, HashMap<Integer, Integer> setScoreMapFirst) throws JSONException {
        JSONArray firstTeamsSets = jsonObject.getJSONArray("scoreTeam").getJSONObject(0).
                getJSONArray("scoreDetails");
        for (int i = 0; i < firstTeamsSets.length(); i++) {
            int setNumber = firstTeamsSets.getJSONObject(i).getInt("setNumber");
            jsonObject.getJSONArray("scoreTeam").getJSONObject(0).
                    getJSONArray("scoreDetails").getJSONObject(i).put("score",
                    setScoreMapFirst.get(setNumber));
        }
    }

    private int getCountOfSets(JSONObject jsonObject) throws JSONException {
        int countOfSets = jsonObject.getJSONArray("scoreTeam").getJSONObject(0).
                getJSONArray("scoreDetails").length();
        return countOfSets;
    }

    private void modifySecondTeamsSets(JSONObject jsonObject, HashMap<Integer, Integer> setScoreMapSecond) throws JSONException {
        JSONArray secondTeamsSets = jsonObject.getJSONArray("scoreTeam").getJSONObject(1).
                getJSONArray("scoreDetails");
        for (int i = 0; i < secondTeamsSets.length(); i++) {
            int setNumber = secondTeamsSets.getJSONObject(i).getInt("setNumber");
            jsonObject.getJSONArray("scoreTeam").getJSONObject(1).
                    getJSONArray("scoreDetails").getJSONObject(i).put("score",
                    setScoreMapSecond.get(setNumber));
        }
    }

    private void updateTeamsFinalScores(JSONObject jsonObject) throws JSONException {
        jsonObject.getJSONArray("scoreTeam").getJSONObject(0).
                put("score", Integer.parseInt(firstScoreET.getText().toString()));
        jsonObject.getJSONArray("scoreTeam").getJSONObject(1).
                put("score", Integer.parseInt(secondScoreET.getText().toString()));
    }

    private boolean isTeam(JSONObject jsonObject) throws JSONException {
        boolean isTeam = false;
        if (jsonObject.getJSONArray("scoreTeam").length() > 0 && jsonObject.getJSONArray("scoreInd").length() == 0)
            isTeam = true;
        return isTeam;
    }

    class ChangeResultsExecute extends AsyncTask<Void, Void, String> {
        final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();
        final HttpContext localContext = globalActivity.getLocalContext();
        private final String urlToSendChangeMatchInfo =
                "http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/referee/setScoreWithResult";
        public ChangeResultsExecute() {  }

        @Override
        protected String doInBackground(Void... params) {
            String ret;
            try {
                HttpClient client = new DefaultHttpClient();
                HttpPost request = new HttpPost(urlToSendChangeMatchInfo);
                String jsonObjectAsString = jsonObject.toString();
                StringEntity stringEntity = new StringEntity(jsonObjectAsString);
                request.setEntity(stringEntity);
                request.setHeader("Accept", "application/json");
                request.setHeader("Content-type", "application/json");
                HttpResponse response = client.execute(request, localContext);
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
            JSONObject jsonResult = null;
            try {
                jsonResult = new JSONObject(result);
                if (jsonResult.getString("status").equals("OK")){
                    Toast.makeText(getBaseContext(), jsonResult.getString("description"), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getBaseContext(), jsonResult.getString("description"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class GetResultsExecute extends AsyncTask<Void, Void, String> {
        private final String urlToGetMatchById =
                "http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/home/getMatchById?id=";
        private int id;
        public GetResultsExecute(int id_) { id = id_; }

        @Override
        protected String doInBackground(Void... params) {
            String ret;
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet(urlToGetMatchById + id);
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
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                try {
                    jsonObject = new JSONObject(result);
                    parseJSONObject(jsonObject);
                    if (matchInfo != null) {
                        firstNameTV.setText(matchInfo.getFirstName());
                        secondNameTV.setText(matchInfo.getSecondName());
                        firstScoreET.setText(String.valueOf(matchInfo.getFirstScore()));
                        secondScoreET.setText(String.valueOf(matchInfo.getSecondScore()));
                    }
                    int i = 0;
                    if (partOfGameListElements == null){
                        partOfGameListElements = new ArrayList();
                    }
                    while (i < gameDetailsFirst.size() && i < gameDetailsSecond.size()){
                        partOfGameListElements.add(new PartOfGameListElement(gameDetailsFirst.get(i).getTeamName(),
                                gameDetailsSecond.get(i).getTeamName(), gameDetailsFirst.get(i).getScore(),
                                gameDetailsSecond.get(i).getScore(), i + 1));
                        i++;
                    }
                    changeResultsAdapter = new ChangeResultsAdapter(ChangeResultsActivity.this, 0, partOfGameListElements);
                    listView.setAdapter(changeResultsAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void parseJSONObject(JSONObject jsonObject) throws JSONException, IOException {
            int firstScore, secondScore;
            String firstName, secondName;
            if (isTeam(jsonObject)) {
                generateTeamMatchInfo(jsonObject);
                generateFirstTeamsScoreDetails(jsonObject);
                generateSecondTeamsScoreDetails(jsonObject);
            } else {
                JSONObject firstUser = jsonObject.getJSONArray("scoreInd").getJSONObject(0).
                        getJSONObject("pk").getJSONObject("player").getJSONObject("user");
                JSONObject secondUser = jsonObject.getJSONArray("scoreInd").getJSONObject(1).
                        getJSONObject("pk").getJSONObject("player").getJSONObject("user");
                firstName = firstUser.getString("name") + " " + firstUser.getString("surname");
                secondName = secondUser.getString("name") + " " + secondUser.getString("surname");
                firstScore = jsonObject.getJSONArray("scoreInd").getJSONObject(0).
                        getInt("score");
                secondScore = jsonObject.getJSONArray("scoreInd").getJSONObject(1).
                        getInt("score");
                matchInfo = new MatchInfo(firstName, secondName, firstScore, secondScore);
                JSONArray scoreDetails = jsonObject.getJSONArray("scoreInd").getJSONObject(0).
                        getJSONArray("scoreDetails");
                if (gameDetailsFirst == null)
                    gameDetailsFirst = new ArrayList<>();
                int i = 0;
                while (i < scoreDetails.length()){
                    int setNumber = scoreDetails.getJSONObject(i).getInt("setNumber");
                    int score = scoreDetails.getJSONObject(i).getInt("score");
                    gameDetailsFirst.add(new PartOfGame(setNumber, firstName, score));
                    i++;
                }
                Collections.sort(gameDetailsFirst);
                if (gameDetailsSecond == null)
                    gameDetailsSecond = new ArrayList<>();
                i = 0;
                scoreDetails = jsonObject.getJSONArray("scoreInd").getJSONObject(1).
                        getJSONArray("scoreDetails");
                while (i < scoreDetails.length()){
                    int setNumber = scoreDetails.getJSONObject(i).getInt("setNumber");
                    int score = scoreDetails.getJSONObject(i).getInt("score");
                    gameDetailsSecond.add(new PartOfGame(setNumber, secondName, score));
                    i++;
                }
                Collections.sort(gameDetailsSecond);
            }
        }

        private void generateSecondTeamsScoreDetails(JSONObject jsonObject) throws JSONException {
            if (gameDetailsSecond == null)
                gameDetailsSecond = new ArrayList<>();
            int i = 0;
            JSONArray scoreDetails = jsonObject.getJSONArray("scoreTeam").getJSONObject(1).
                    getJSONArray("scoreDetails");
            while (i < scoreDetails.length()){
                int setNumber = scoreDetails.getJSONObject(i).getInt("setNumber");
                int score = scoreDetails.getJSONObject(i).getInt("score");
                String teamName = scoreDetails.getJSONObject(i).getJSONObject("team").getString("name");
                gameDetailsSecond.add(new PartOfGame(setNumber, teamName, score));
                i++;
            }
            Collections.sort(gameDetailsSecond);
        }

        private void generateFirstTeamsScoreDetails(JSONObject jsonObject) throws JSONException {
            JSONArray scoreDetails = jsonObject.getJSONArray("scoreTeam").getJSONObject(0).
                    getJSONArray("scoreDetails");
            if (gameDetailsFirst == null)
                gameDetailsFirst = new ArrayList<>();
            int i = 0;
            while (i < scoreDetails.length()){
                int setNumber = scoreDetails.getJSONObject(i).getInt("setNumber");
                int score = scoreDetails.getJSONObject(i).getInt("score");
                String teamName = scoreDetails.getJSONObject(i).getJSONObject("team").getString("name");
                gameDetailsFirst.add(new PartOfGame(setNumber, teamName, score));
                i++;
            }
            Collections.sort(gameDetailsFirst);
        }

        private void generateTeamMatchInfo(JSONObject jsonObject) throws JSONException {
            String firstName;
            String secondName;
            int firstScore;
            int secondScore;
            firstName = jsonObject.getJSONArray("scoreTeam").getJSONObject(0).
                    getJSONObject("pk").getJSONObject("team").getString("name");
            secondName = jsonObject.getJSONArray("scoreTeam").getJSONObject(1).
                    getJSONObject("pk").getJSONObject("team").getString("name");
            firstScore = jsonObject.getJSONArray("scoreTeam").getJSONObject(0).
                    getInt("score");
            secondScore = jsonObject.getJSONArray("scoreTeam").getJSONObject(1).
                    getInt("score");
            matchInfo = new MatchInfo(firstName, secondName, firstScore, secondScore);
        }
    }
}

class MatchInfo {
    MatchInfo(String firstName_, String secondName_, int firstScore_, int secondScore_){
        firstName = firstName_;
        secondName = secondName_;
        firstScore = firstScore_;
        secondScore = secondScore_;
    }
    private int firstScore, secondScore;
    private String firstName, secondName;
    public int getFirstScore() { return firstScore; }
    public int getSecondScore() { return secondScore; }
    public String getFirstName() { return firstName; }
    public String getSecondName() { return secondName; }
}

class PartOfGame implements Comparable<PartOfGame> {
    public int getPartNumber() {
        return partNumber;
    }

    public String getTeamName() {
        return teamName;
    }

    public int getScore() {
        return score;
    }

    int partNumber, score;
    String teamName;
    PartOfGame (int partNumber_, String teamName_, int score_ ){
        partNumber = partNumber_;
        teamName = teamName_;
        score = score_;
    }

    @Override
    public int compareTo(PartOfGame another) {
        int comparison = this.partNumber - another.partNumber;
        return comparison;
    }
}

class PartOfGameListElement {
    String firstTeamName, secondTeamName;
    int firstTeamScore, secondTeamScore, setNumber;
    public PartOfGameListElement(String firstTeamName_, String secondTeamName_,
                                 int firstTeamScore_, int secondTeamScore_, int setNumber_) {
        firstTeamName = firstTeamName_;
        secondTeamName = secondTeamName_;
        firstTeamScore = firstTeamScore_;
        secondTeamScore = secondTeamScore_;
        setNumber = setNumber_;
    }
    public String getFirstTeamName() { return firstTeamName; }
    public String getSecondTeamName() { return secondTeamName; }
    public int getFirstTeamScore() { return firstTeamScore; }
    public int getSecondTeamScore() { return secondTeamScore; }
    public int getSetNumber() { return  setNumber; }
    public void setFirstTeamScore(int firstTeamScore) { this.firstTeamScore = firstTeamScore; }
    public void setSecondTeamScore(int secondTeamScore) { this.secondTeamScore = secondTeamScore; }
}

class ChangeResultsAdapter extends ArrayAdapter<PartOfGameListElement> {
    private ArrayList<PartOfGameListElement> listOfElements;
    private static LayoutInflater inflater = null;

    public ChangeResultsAdapter (Activity activity, int textViewResourceId,ArrayList<PartOfGameListElement> element) {
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
        public TextView setNumber;
        public TextView firstTeamName;
        public TextView secondTeamName;
        public EditText firstTeamScore;
        public EditText secondTeamScore;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.change_results_list_element, parent, false);
                holder = new ViewHolder();
                holder.setNumber = (TextView) vi.findViewById(R.id.setLabelTV);
                holder.firstTeamName = (TextView) vi.findViewById(R.id.elemFirstTeamNameTV);
                holder.secondTeamName = (TextView) vi.findViewById(R.id.elemSecondTeamNameTV);
                holder.firstTeamScore = (EditText) vi.findViewById(R.id.firstTeamScore);
                holder.secondTeamScore = (EditText) vi.findViewById(R.id.secondTeamScore);
                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }
            int setNumber = position + 1;
            holder.setNumber.setText("Set " + setNumber);
            holder.firstTeamName.setText(listOfElements.get(position).getFirstTeamName());
            holder.secondTeamName.setText(listOfElements.get(position).getSecondTeamName());
            holder.firstTeamScore.setText(String.valueOf(listOfElements.get(position).getFirstTeamScore()));
            holder.firstTeamScore.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!holder.firstTeamScore.getText().toString().isEmpty())
                        listOfElements.get(position).setFirstTeamScore(Integer.parseInt(holder.firstTeamScore.getText().toString()));
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            holder.secondTeamScore.setText(String.valueOf(listOfElements.get(position).getSecondTeamScore()));
            holder.secondTeamScore.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!holder.secondTeamScore.getText().toString().isEmpty())
                        listOfElements.get(position).setSecondTeamScore(Integer.parseInt(holder.secondTeamScore.getText().toString()));
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        } catch (Exception e) { }

        return vi;
    }
}

