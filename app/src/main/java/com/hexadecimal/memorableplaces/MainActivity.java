package com.hexadecimal.memorableplaces;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;

    // diger siniftan erisebilmek icin static olarak belirledik
    static ArrayList<String> places = new ArrayList<String>();
    // kaydedecegimiz yerleri ekleyecegimiz arraylist
    static ArrayList<LatLng> locations = new ArrayList<LatLng>();

    static ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);

        places.add("Add a new place...");
        locations.add(new LatLng(0,0));

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, places);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                // putExtra() metodunu gidelen aktiviteye ekstra bilgi aktarmak icin kullandik
                intent.putExtra("place number", i);
                startActivity(intent);
            }
        });
    }
}
