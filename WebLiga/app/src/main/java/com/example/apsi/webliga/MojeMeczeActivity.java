package com.example.apsi.webliga;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class MojeMeczeActivity extends AppCompatActivity {

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moje_mecze);
        initToolBar();

        //do sprawdzenia jakiego typu jest użytkownik
        final GlobalActivity globalActivity = (GlobalActivity) getApplicationContext();

        if(globalActivity.getIsCapitan()=="Y"){
            final Button buttonKapitan = (Button) findViewById(R.id.buttonKapitan);
            buttonKapitan.setVisibility(View.VISIBLE);
        }
        if(globalActivity.getIsPlayer()=="Y"){
            final Button buttonGracz = (Button) findViewById(R.id.buttonGracz);
            buttonGracz.setVisibility(View.VISIBLE);
        }
        if(globalActivity.getIsReferee()=="Y"){
            final Button buttonSedzia = (Button) findViewById(R.id.buttonSedzia);
            buttonSedzia.setVisibility(View.VISIBLE);
        }
        if(globalActivity.getIsOrganizer()=="Y"){
            final Button buttonOrganizator = (Button) findViewById(R.id.buttonOrganizator);
            buttonOrganizator.setVisibility(View.VISIBLE);
        }

        final Spinner spinner = (Spinner) findViewById(R.id.spinnerToolbar2);
        //  String[] elementy = {"","Menu", "Wyszukaj ligę", "Wyszukaj zespół","Moje mecze"};
        ArrayList<String> arrayList1 = new ArrayList<String>();
        //elementy menu, które będą nie zależnie od typu użytkownika; z jakiegos powodu zapetlenie aktywnosci
        arrayList1.add("Menu");
        arrayList1.add("Menu");
        arrayList1.add("Wyszukaj ligę");
        arrayList1.add("Wyszukaj zespół");
        arrayList1.add("Moje mecze");
        // ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, arrayList1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, arrayList1){

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent)
            {
                View v = null;

                // If this is the initial dummy entry, make it hidden
                if (position == 0) {
                    TextView tv = new TextView(getContext());
                    tv.setHeight(0);
                    tv.setVisibility(View.GONE);
                    v = tv;
                }
                else {
                    // Pass convertView as null to prevent reuse of special case views
                    v = super.getDropDownView(position, null, parent);
                }

                // Hide scroll bar because it appears sometimes unnecessarily, this does not prevent scrolling
                parent.setVerticalScrollBarEnabled(false);
                return v;
            }
        };

        adapter.setDropDownViewResource(R.layout.spinner_item);
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
                        //przycisk menu który otwiera główną stronę
                        startActivity(new Intent(MojeMeczeActivity.this, AfterLoginActivity.class));
                        spinner.setSelection(0);
                        break;
                    case 2:
                        //przycisk wyszukaj ligę, który otwiera stronę wyszukiwania ligi
                        startActivity(new Intent(MojeMeczeActivity.this, SearchLeagueActivity.class));
                        spinner.setSelection(0);
                        break;
                    case 3:
                        //przycisk wyszukaj zespół, który otwiera stronę wyszukiwania zespołu
                        startActivity(new Intent(MojeMeczeActivity.this, SearchTeamActivity.class));
                        spinner.setSelection(0);
                        break;
                    case 4:
                        //przycisk do otwierania moich meczów
                        startActivity(new Intent(MojeMeczeActivity.this, MojeMeczeActivity.class));
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

    public void wyloguj(final View view) {
        startActivity(new Intent(MojeMeczeActivity.this, MainActivity.class));
    }
}
