package com.example.melody.heapsortandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.lang.reflect.Array;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, 1);
                }
            }

            protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                if (requestCode == 1 && resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                }
            }

            String trashCategories() {

                String[] recycle = {"bottle", "can"};
                String[] compost = {"food"};
                String[] landfill = ["", ""];

                int recycleCount = 0;
                int compostCount = 0;
                int landfillCount = 0;

                String[] tagNames;
                int[] tagScores =;

                //put tag names in tagNames
                for (int i = 0; i < tags[].Name.size(); i++) {
                    tagNames[i] = tags[i].Name;
                }

                //put tag scores in tagScores
                for (int i = 0; i < tags[].Score.size(); i++) {
                    tagScores[i] = tags[i].Score;
                }

                for (int i = 0; i < tagNames.size(); i++) {
                    if (tagNames[i] == recycle[i]) {
                        recycleCount++;
                    } else if (tagNames[i] == compost[i]) {
                        compostCount++;
                    } else if (tagNames[i] == landfill[i]) {
                        landfillCount++;
                    }
                }

                if (recycleCount > compostCount && recycleCount > landfillCount) {
                    return "Please throw into the RECYCLE bin.";
                } else if (compostCount > recycleCount && compostCount > landfillCount) {
                    return "Please throw into the COMPOST bin.";
                } else
                    return "Please throw into the LANDFILL bin.";
            }


        });
    }

}
