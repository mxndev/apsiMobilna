package com.example.apsi.webliga;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MyTeamsActivity extends AppCompatActivity {

    private ArrayList<SearchTeamListElement> searchTeamListElements;
    private SearchTeamAdapter searchTeamAdapter;
    private ListView listView;

    public void newTeam(View view) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.create_team_element, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = (EditText) dialogView.findViewById(R.id.editText);

        dialogBuilder.setTitle("Tworzenie nowej drużyny");
        dialogBuilder.setMessage("Tutaj możesz dodać nowy zespół");
        dialogBuilder.setPositiveButton("Stwórz", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                new HttpAsyncTask().execute("http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/player/createTeam", edt.getText().toString());
            }
        });
        dialogBuilder.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(getBaseContext(), "Anulowano!", Toast.LENGTH_LONG).show();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public String POST(String url, String name){
        String response = "";
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            String json = "{\"id\":null,\"capitan\":null,\"name\":\""+ name.trim() +"\",\"players\":[],\"league\":null}";
            StringEntity se = new StringEntity(json);
            httpPost.setEntity(se);
            httpPost.setHeader("Content-type", "application/json");

            final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();
            final HttpContext localContext = globalActivity.getLocalContext();
            HttpResponse httpResponse = httpclient.execute(httpPost, localContext);

            if(httpResponse.getStatusLine().getStatusCode() == 200)
                response = "OK";
            else
                response = "Error";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return response;
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls)
        {
            return POST(urls[0], urls[1]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if(result.equals("OK" ))
                Toast.makeText(getBaseContext(), "Zespół został utworzony!", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getBaseContext(), "Nie udało się utworzyć zespołu!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_teams);
        listView = (ListView) findViewById(R.id.listView2);
        final Button button = (Button) findViewById(R.id.searchTeamSLBtn);

        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Object o = listView.getItemAtPosition(position);
                SearchTeamListElement obj_itemDetails = (SearchTeamListElement)o;
                Intent intent = new Intent(MyTeamsActivity.this, TeamActivity.class);
                intent.putExtra("ID", obj_itemDetails.getTeamID());
                startActivity(intent);
            }
        };
        listView.setOnItemClickListener(listener);
        class SearchTeamExecute extends AsyncTask<Void, Void, String> {
            private final String urlToGetTeamByName =
                    "http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/player/getTeams";

            @Override
            protected String doInBackground(Void... params) {
                String ret;
                try {
                    final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();
                    final HttpContext localContext = globalActivity.getLocalContext();
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet(urlToGetTeamByName);
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
            protected void onPostExecute(String result){
                JSONArray jsonArray;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    try {
                        jsonArray = new JSONArray(result);
                        searchTeamListElements = parseJSONArray(jsonArray);
                        searchTeamAdapter = new SearchTeamAdapter(MyTeamsActivity.this,0,searchTeamListElements);
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
        new SearchTeamExecute().execute();
    }
}

class SearchMyTeamListElement {

    private String teamName;
    private int teamID;

    SearchMyTeamListElement (String teamName_, int teamID_){
        teamID = teamID_;
        teamName = teamName_;
    }
    public String getTeamName() { return teamName; }
    public int getTeamID() { return teamID; }
}

class SearchMyTeamAdapter extends ArrayAdapter<SearchMyTeamListElement> {
    private Activity activity;
    private ArrayList<SearchMyTeamListElement> listOfElements;
    private static LayoutInflater inflater = null;

    public SearchMyTeamAdapter (Activity activity, int textViewResourceId,ArrayList<SearchMyTeamListElement> element) {
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