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

public class TerminarzActivity extends AppCompatActivity {
    ListView listView;
    TerminarzAdapter terminarzAdapter;
    private ArrayList<TerminarzListElement> terminarzListElements;

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
                        terminarzListElements = parseJSONArray(jsonArray);
                        terminarzAdapter = new TerminarzAdapter(TerminarzActivity.this, 0, terminarzListElements);
                        listView.setAdapter(terminarzAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            private ArrayList<TerminarzListElement> parseJSONArray(JSONArray jsonArray) throws JSONException {
                int i = 0;
                ArrayList<TerminarzListElement> groupListElements = new ArrayList<>();
                while (i < jsonArray.length()) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String city = jsonObject.getJSONObject("place").getString("city");
                    String address = jsonObject.getJSONObject("place").getString("address");
                    String date = jsonObject.getString("date");
                    groupListElements.add(new TerminarzListElement(city, address, date));
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

class TerminarzAdapter extends ArrayAdapter<TerminarzListElement> {
    private ArrayList<TerminarzListElement> listOfElements;
    private static LayoutInflater inflater = null;

    public TerminarzAdapter (Activity activity, int textViewResourceId, ArrayList<TerminarzListElement> element) {
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
        public TextView city;
        public TextView address;
        public TextView date;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.terminarz_list_element, null);
                holder = new ViewHolder();
                holder.city = (TextView) vi.findViewById(R.id.cityLabelTV);
                holder.address = (TextView) vi.findViewById(R.id.addressLabelTV);
                holder.date = (TextView) vi.findViewById(R.id.dateLabelTV);
                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }
            holder.city.setText(listOfElements.get(position).getCity());
            holder.address.setText(listOfElements.get(position).getAddress());
            holder.date.setText(listOfElements.get(position).getDate());
        } catch (Exception e) { }

        return vi;
    }
}

class TerminarzListElement {
    private String city, address, date;

    public TerminarzListElement(String city_, String address_, String date_) {
        city = city_;
        address = address_;
        date = date_;
    }
    public String getCity() { return city; }
    public String getAddress() { return address; }
    public String getDate() { return date; }
}