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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MyLeaguesActivity extends AppCompatActivity {
    ArrayList<MyLeaguesElement> leaguesElements;
    ListView myLeaguesLV;
    MyLeaguesAdapter myLeaguesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_leagues);
        new SearchMyLeagueExecute().execute();
    }

    public void addNewLeague(View view) {
        Intent intent = new Intent(MyLeaguesActivity.this, CreateLeagueActivity.class);
        startActivity(intent);
    }

    public void deleteLeague(View view) {
//        ListView listview1 = (ListView) findViewById(R.id.myLeagueList);
//        final int position = listview1.getPositionForView((View) view.getParent());
//        view.getParent().
        class DeleteLeagueExecute extends AsyncTask<Void, Void, String> {
            final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();
            final HttpContext localContext = globalActivity.getLocalContext();
            int leagueId;
            private final String urlToDeleteLeague =
                    "http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/organizer/removeLeague?id=";

            public DeleteLeagueExecute(int leagueId) {
                this.leagueId = leagueId;
            }

            @Override
            protected String doInBackground(Void... params) {
                String ret;
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet(urlToDeleteLeague + leagueId);
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
            protected void onPostExecute(String result) {
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(result);
                    String message = jsonObject.getString("description");
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
//        new DeleteLeagueExecute(leagueId);
    }


    class SearchMyLeagueExecute extends AsyncTask<Void, Void, String> {
        final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();
        final HttpContext localContext = globalActivity.getLocalContext();
        private final String urlToGetMyLeagues =
                "http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/organizer/getMyLeagues";

        public SearchMyLeagueExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            String ret;
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet(urlToGetMyLeagues);
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
        protected void onPostExecute(String result) {
            JSONArray jsonArray;
            try {
                jsonArray = new JSONArray(result);
                if (leaguesElements == null)
                    leaguesElements = new ArrayList<>();
                int i = 0;
                while (i < jsonArray.length()) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String leagueName = jsonObject.getString("name");
                    int leagueId = jsonObject.getInt("id");
                    String sportName = jsonObject.getJSONObject("sport").getString("name");
                    leaguesElements.add(new MyLeaguesElement(leagueName, sportName, leagueId));
                    i++;
                }
                myLeaguesLV = (ListView) findViewById(R.id.myLeagueList);
                myLeaguesAdapter = new MyLeaguesAdapter(MyLeaguesActivity.this, 0, leaguesElements);
                myLeaguesLV.setAdapter(myLeaguesAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

    class MyLeaguesElement {
        private String leagueName, sportName;

        public int getLeagueId() {
            return leagueId;
        }

        private int leagueId;

        public MyLeaguesElement(String leagueName, String sportName, int leagueId) {
            this.leagueName = leagueName;
            this.sportName = sportName;
            this.leagueId = leagueId;
        }

        public String getLeagueName() {
            return leagueName;
        }

        public String getSportName() {
            return sportName;
        }

    }

    class MyLeaguesAdapter extends ArrayAdapter<MyLeaguesElement> {
        private ArrayList<MyLeaguesElement> listOfElements;
        private static LayoutInflater inflater = null;

        public MyLeaguesAdapter(Activity activity, int textViewResourceId, ArrayList<MyLeaguesElement> element) {
            super(activity, textViewResourceId, element);
            try {
                this.listOfElements = element;
                inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            } catch (Exception e) {
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
            public TextView leagueName;
            public TextView sportName;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            final ViewHolder holder;
            try {
                if (convertView == null) {
                    vi = inflater.inflate(R.layout.my_league_list_item, parent, false);
                    holder = new ViewHolder();
                    holder.leagueName = (TextView) vi.findViewById(R.id.myLeagueNameTV);
                    holder.sportName = (TextView) vi.findViewById(R.id.mySportNameTV);
                    vi.setTag(holder);
                } else {
                    holder = (ViewHolder) vi.getTag();
                }
                holder.leagueName.setText(listOfElements.get(position).getLeagueName());
                holder.sportName.setText(listOfElements.get(position).getSportName());
                holder.leagueName.setTag(position, listOfElements.get(position).getLeagueId());

            } catch (Exception e) {
            }

            return vi;
        }
    }



