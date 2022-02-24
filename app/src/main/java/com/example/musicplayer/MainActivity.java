package com.example.musicplayer;
import androidx.appcompat.app.AppCompatActivity;


import android.Manifest;

import android.content.Intent;

import android.graphics.Color;

import android.os.Bundle;
import android.os.Environment;

import android.view.View;

import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        ArrayList<File>mySongs = fetchSongs(Environment.getExternalStorageDirectory());
                        String [] items = new String[mySongs.size()];
                        for (int i =0; i<mySongs.size();i++){
                            items[i] = mySongs.get(i).getName().replace(".mp3","");
                        }
                        List<String> songsName = new ArrayList<>(Arrays.asList(items));
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                                (MainActivity.this, android.R.layout.simple_list_item_1, songsName){
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent){
                                // Get the Item from ListView
                                View view = super.getView(position, convertView, parent);

                                // Initialize a TextView for ListView each Item
                                TextView tv = view.findViewById(android.R.id.text1);

                                // Set the text color of TextView (ListView Item)
                                tv.setTextColor(Color.WHITE);

                                // Generate ListView Item using TextView
                                return view;
                            }
                        };

                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                                Intent intent = new Intent(MainActivity.this,PlaySong.class);
                                String currentSong = listView.getItemAtPosition(position).toString();
                                intent.putExtra("songList",mySongs);
                                intent.putExtra("currentSong",currentSong);
                                intent.putExtra("position",position);
                                startActivity(intent);
                            }
                        });

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();
    }

    // Function that fetches mp3 files from the user's phone.
    public ArrayList<File>fetchSongs(File file){
        ArrayList arrayList = new ArrayList();
        File[] songs = file.listFiles();
        if (songs!=null){
            for (File myFile:songs){
                if (!myFile.isHidden() && myFile.isDirectory()){
                    arrayList.addAll(fetchSongs(myFile));
                }
                else{
                    if (myFile.getName().endsWith(".mp3") && !myFile.getName().startsWith(".") && !myFile.getName().startsWith("AUD") && !myFile.getName().startsWith("tone")){
                        arrayList.add(myFile);
                    }
                }
            }
        }
        return arrayList;
    }

}