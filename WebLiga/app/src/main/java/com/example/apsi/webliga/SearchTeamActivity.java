package com.example.apsi.webliga;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

public class SearchTeamActivity extends AppCompatActivity {

    private ArrayList<SearchTeamListElement> searchTeamListElements;
    private SearchTeamAdapter searchTeamAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_team);
        listView = (ListView) findViewById(R.id.listView2);
        final Button button = (Button) findViewById(R.id.searchTeamSLBtn);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            if (button != null) {
                button.callOnClick();
            }
        }
        EditText editText = (EditText) findViewById(R.id.teamNameET);
        if (editText != null) {
            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                            (actionId == EditorInfo.IME_ACTION_DONE)) {
                        if (button != null) {
                            button.callOnClick();
                        }
                    }
                    return false;
                }
            });
        }
    }

    public void searchTeam(View view) {
        class SearchTeamExecute extends AsyncTask<Void, Void, String> {
            private String teamName;
            private final String urlToGetTeamByName =
                    "http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/home/getTeamsByName?name=";

            public SearchTeamExecute(String teamName_) {
                teamName = teamName_;
            }

            @Override
            protected String doInBackground(Void... params) {
                String ret;
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet(urlToGetTeamByName + teamName);
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
                        searchTeamListElements = parseJSONArray(jsonArray);
                        searchTeamAdapter = new SearchTeamAdapter(SearchTeamActivity.this,0,searchTeamListElements);
                        listView.setAdapter(searchTeamAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            private ArrayList<SearchTeamListElement> parseJSONArray(JSONArray jsonArray) throws JSONException {
                int i = 0;
                ArrayList<SearchTeamListElement> searchTeamListElements = new ArrayList<>();
                while (i < jsonArray.length()) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int teamID = jsonObject.getInt("id");
                    String teamName = jsonObject.getString("name");
                    searchTeamListElements.add(new SearchTeamListElement(teamName, teamID));
                    i++;
                }
                return searchTeamListElements;
            }
        }
        String teamName = ((EditText) findViewById(R.id.teamNameET)).getText().toString().trim();
        new SearchTeamExecute(teamName).execute();
        view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}

class SearchTeamListElement {

    private String teamName;
    private int teamID;

    SearchTeamListElement (String teamName_, int teamID_){
        teamID = teamID_;
        teamName = teamName_;
    }
    public String getTeamName() { return teamName; }
    public int getTeamID() { return teamID; }
}

class SearchTeamAdapter extends ArrayAdapter<SearchTeamListElement> {
    private Activity activity;
    private ArrayList<SearchTeamListElement> listOfElements;
    private static LayoutInflater inflater = null;

    public SearchTeamAdapter (Activity activity, int textViewResourceId,ArrayList<SearchTeamListElement> element) {
        super(activity, textViewResourceId, element);
        try {
            this.activity = activity;
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
                vi = inflater.inflate(R.layout.search_team_list_element, null);
                holder = new ViewHolder();
                holder.teamName = (TextView) vi.findViewById(R.id.teamNameTV);
                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }
            holder.teamName.setText(listOfElements.get(position).getTeamName());
        } catch (Exception e) { }
        return vi;
    }
}