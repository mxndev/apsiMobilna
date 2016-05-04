package com.example.apsi.webliga;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;


public class MainWindowActivity extends AppCompatActivity {

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_window);
        initToolBar();

        final Spinner spinner = (Spinner) findViewById(R.id.spinnerToolbar);
        String[] elementy = {"Menu", "Wyszukaj ligę", "Wyszukaj zespół"};
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.spinner_item, elementy);

        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int id, long position) {

                switch((int)position)
                {
                    case 0:


                        break;
                    case 1:

                        startActivity(new Intent(MainWindowActivity.this, SearchLeagueActivity.class));
                        spinner.setSelection(0);
                        break;
                    case 2:

                        startActivity(new Intent(MainWindowActivity.this, SearchTeamActivity.class));
                        spinner.setSelection(0);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }

        });
    }

    public void initToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.toolbarTitle);
        }
    }

    public void login(final View view) {
        startActivity(new Intent(MainWindowActivity.this, MainActivity.class));
    }

    public void zarejestruj(final View view) {
        startActivity(new Intent(MainWindowActivity.this, RegisterActivity.class));
    }



}
