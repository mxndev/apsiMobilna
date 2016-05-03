package com.example.apsi.webliga;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RegisterActivity extends AppCompatActivity {

    EditText login,haslo,imie,nazwisko;
    CheckBox graczRegister,kapitanRegister,organizatorRegister,sedziaRegister;
    String gracz=null,kapitan=null,organizator=null,sedzia=null,admin=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //referencje do obiektow
        login = (EditText) findViewById(R.id.login);
        haslo = (EditText) findViewById(R.id.haslo);
        imie = (EditText) findViewById(R.id.imie);
        nazwisko = (EditText) findViewById(R.id.nazwisko);
        graczRegister = (CheckBox) findViewById(R.id.graczRegister);
        kapitanRegister = (CheckBox) findViewById(R.id.kapitanRegister);
        organizatorRegister = (CheckBox) findViewById(R.id.organizatorRegister);
        sedziaRegister = (CheckBox) findViewById(R.id.sedziaRegister);
    }

    public void anuluj(final View view) {
        startActivity(new Intent(RegisterActivity.this, MainWindowActivity.class));
    }

    public void zarejestruj(final View view) {
        new HttpAsyncTask().execute("http://multiliga-mrzeszotarski.rhcloud.com/multiliga/api/createUser");
        startActivity(new Intent(RegisterActivity.this, MainWindowActivity.class));
    }

    public String POST(String url){
        InputStream inputStream = null;
        String result = "";
        try {
            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json = "";

            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("login", login.getText().toString().trim());
            jsonObject.accumulate("password", haslo.getText().toString().trim());
            jsonObject.accumulate("name", imie.getText().toString().trim());
            jsonObject.accumulate("surname", nazwisko.getText().toString().trim());
            if(graczRegister.isChecked()){
                gracz="Y";
                jsonObject.accumulate("isPlayer",gracz);
            }
            else{
                jsonObject.accumulate("isPlayer", JSONObject.NULL);
            }
            if(sedziaRegister.isChecked()){
                sedzia="Y";
                jsonObject.accumulate("isReferee", sedzia);
            }
            else{
                jsonObject.accumulate("isReferee", JSONObject.NULL);
            }
            if(kapitanRegister.isChecked()){
                kapitan="Y";
                jsonObject.accumulate("isCapitan", kapitan);
            }
            else{
                jsonObject.accumulate("isCapitan", JSONObject.NULL);
            }
            jsonObject.accumulate("isAdmin",JSONObject.NULL );
            if(organizatorRegister.isChecked()){
                organizator="Y";
                jsonObject.accumulate("isOrganizer", organizator);
            }
            else{
                jsonObject.accumulate("isOrganizer", JSONObject.NULL);
            }

            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();

            // ** Alternative way to convert Person object to JSON string usin Jackson Lib
            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(person);

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
           // httpPost.setHeader("Accept", "application/json");
           // httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String stringResponse="";

            return stringResponse = POST(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
            Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
        }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}
