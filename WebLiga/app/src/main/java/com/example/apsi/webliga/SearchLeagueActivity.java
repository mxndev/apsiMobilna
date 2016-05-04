package com.example.apsi.webliga;

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

public class SearchLeagueActivity extends AppCompatActivity {
    ListView listView;
    SearchLeagueAdapter searchLeagueAdapter;
    private ArrayList<SearchLeagueListElement> searchLeagueListElements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_league);
        listView = (ListView) findViewById(R.id.listView);
        final Button button = (Button) findViewById(R.id.searchLeagueSLBtn);
        clickButton(button);
        EditText editText = (EditText) findViewById(R.id.leagueNameET);
        addListenerToEditText(button, editText);
    }

    private void addListenerToEditText(final Button button, EditText editText) {
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (actionId == EditorInfo.IME_ACTION_DONE)) {
                    clickButton(button);
                }
                return false;
            }
        });
    }

    private void clickButton(Button button) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            if (button != null) {
                button.callOnClick();
            }
        }
    }

    public void searchLeague(View view) {
        class SearchLeagueExecute extends AsyncTask<Void, Void, String> {
            private String leagueName;
            private final String urlToGetLeagueByName =
                    "http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/home/getLeaguesByName?name=";

            public SearchLeagueExecute(String leagueName_) {
                leagueName = leagueName_;
            }

            @Override
            protected String doInBackground(Void... params) {
                String ret;
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet(urlToGetLeagueByName + leagueName);
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
                        searchLeagueListElements = parseJSONArray(jsonArray);
                        searchLeagueAdapter = new SearchLeagueAdapter(SearchLeagueActivity.this, 0, searchLeagueListElements);
                        listView.setAdapter(searchLeagueAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            private ArrayList<SearchLeagueListElement> parseJSONArray(JSONArray jsonArray) throws JSONException {
                int i = 0;
                ArrayList<SearchLeagueListElement> searchLeagueListElements = new ArrayList<>();
                while (i < jsonArray.length()) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int leagueID = jsonObject.getInt("id");
                    String organizerName = jsonObject.getJSONObject("organizer").getJSONObject("user").getString("name") +
                            " " + jsonObject.getJSONObject("organizer").getJSONObject("user").getString("surname");
                    String leagueName = jsonObject.getString("name");
                    String sportName = jsonObject.getJSONObject("sport").getString("name");
                    searchLeagueListElements.add(new SearchLeagueListElement(leagueName,organizerName,sportName, leagueID));
                    i++;
                }
                return searchLeagueListElements;
            }
        }
        String leagueName = ((EditText) findViewById(R.id.leagueNameET)).getText().toString().trim();
        new SearchLeagueExecute(leagueName).execute();
        view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }
}

class SearchLeagueAdapter extends ArrayAdapter<SearchLeagueListElement> {
    private ArrayList<SearchLeagueListElement> listOfElements;
    private static LayoutInflater inflater = null;

    public SearchLeagueAdapter (Activity activity, int textViewResourceId,ArrayList<SearchLeagueListElement> element) {
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
        public TextView leagueName;
        public TextView sportName;
        public TextView organizer;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.search_league_list_element, null);
                holder = new ViewHolder();
                holder.leagueName = (TextView) vi.findViewById(R.id.leagueNameTV);
                holder.sportName = (TextView) vi.findViewById(R.id.sportNameTV);
                holder.organizer = (TextView) vi.findViewById(R.id.organizerTV);
                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }
            holder.leagueName.setText(listOfElements.get(position).getLeagueName());
            holder.leagueName.setTag(listOfElements.get(position).getLeagueID());
            holder.organizer.setText(listOfElements.get(position).getOrganizer());
            holder.sportName.setText(listOfElements.get(position).getSportName());
        } catch (Exception e) { }
        return vi;
    }
}

class SearchLeagueListElement {
    private String leagueName, organizer, sportName;
    private int leagueID;

    public SearchLeagueListElement(String leagueName_, String organizer_, String sportName_, int leagueID_) {
        leagueName = leagueName_;
        organizer = organizer_;
        sportName = sportName_;
        leagueID = leagueID_;
    }
    public String getLeagueName() { return leagueName; }
    public String getOrganizer() { return organizer; }
    public String getSportName() { return sportName; }
    public int getLeagueID() { return leagueID; }
}