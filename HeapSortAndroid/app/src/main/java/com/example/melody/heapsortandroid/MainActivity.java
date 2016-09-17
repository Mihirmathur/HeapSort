package com.example.melody.heapsortandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;

public class MainActivity extends AppCompatActivity {

    ImageView thumbnail;
    Button clickme;
    private VisionServiceClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        thumbnail = (ImageView) findViewById(R.id.thumbnail);
        clickme = (Button) findViewById(R.id.clickme);

        setSupportActionBar(toolbar);
        if (client==null){
            client = new VisionServiceRestClient(getString(R.string.subscription_key));
        }


        clickme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, 1);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            thumbnail.setImageBitmap(imageBitmap);
            clickme.setText("");
            new ComputerVision().execute(imageBitmap);
        }
    }

    private String getAnalysisResult(Bitmap b) throws VisionServiceException, IOException {
        Gson gson = new Gson();
        String[] features = {"Categories"};
        String[] details = {};

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        AnalysisResult v =  this.client.analyzeImage(inputStream, features, details);
        return gson.toJson(v);
    }

    private class ComputerVision extends AsyncTask<Bitmap, Void, String> {

        @Override
        protected String doInBackground(Bitmap... params) {
            try {
                return getAnalysisResult(params[0]);
            } catch (VisionServiceException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            clickme.setText(result);
        }
    }

//    String trashCategories() {
//
//        String[] recycle = {"bottle", "can"};
//        String[] compost = {"food"};
//        String[] landfill = {"", ""};
//
//        int recycleCount = 0;
//        int compostCount = 0;
//        int landfillCount = 0;
//
//        String[] tagNames;
//        int[] tagScores;
//
//        //put tag names in tagNames
//        for (int i = 0; i < tags[].Name.size(); i++) {
//            tagNames[i] = tags[i].Name;
//        }
//
//        //put tag scores in tagScores
//        for (int i = 0; i < tags[].Score.size(); i++) {
//            tagScores[i] = tags[i].Score;
//        }
//
//        for (int i = 0; i < tagNames.size(); i++) {
//            if (tagNames[i] == recycle[i]) {
//                recycleCount++;
//            } else if (tagNames[i] == compost[i]) {
//                compostCount++;
//            } else if (tagNames[i] == landfill[i]) {
//                landfillCount++;
//            }
//        }
//
//        if (recycleCount > compostCount && recycleCount > landfillCount) {
//            return "Please throw into the RECYCLE bin.";
//        } else if (compostCount > recycleCount && compostCount > landfillCount) {
//            return "Please throw into the COMPOST bin.";
//        } else
//            return "Please throw into the LANDFILL bin.";
//    }

}
