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
import android.widget.TextView;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    ImageView thumbnail;
    Button clickme;
    TextView results;
    private VisionServiceClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        thumbnail = (ImageView) findViewById(R.id.thumbnail);
        clickme = (Button) findViewById(R.id.clickme);
        results = (TextView) findViewById(R.id.results);

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

    private JSONArray getAnalysisResult(Bitmap b) throws VisionServiceException, IOException, JSONException {
        Gson gson = new Gson();
        String[] features = {"Categories"};
        String[] details = {};

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        AnalysisResult v =  this.client.analyzeImage(inputStream, features, details);
        return (JSONArray) new JSONObject(gson.toJson(v)).get("categories");
    }

    private class ComputerVision extends AsyncTask<Bitmap, Void, JSONArray> {

        @Override
        protected JSONArray doInBackground(Bitmap... params) {
            try {
                return getAnalysisResult(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            clickme.setText(result.toString());
            results.setText(trashCategories(result));
        }
    }

    String trashCategories(JSONArray categories) {

        ArrayList<String> recycle = new ArrayList<String>(Arrays.asList("bottle", "can", "drink"));
        ArrayList<String> compost = new ArrayList<String>(Arrays.asList("food"));
        ArrayList<String> landfill = new ArrayList<String>();

        int recycleCount = 0;
        int compostCount = 0;
        int landfillCount = 0;

        ArrayList<String> tagNames = new ArrayList<String>();

        //put tag names in tagNames
        for (int i = 0; i < categories.length(); i++) {
            try {
                System.out.println(categories.getJSONObject(i));
                tagNames.add(categories.getJSONObject(i).getString("name"));
                System.out.println(tagNames.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < tagNames.size(); i++) {
            if (recycle.contains(tagNames.get(i))) {
                recycleCount++;
            }
            if (compost.contains(tagNames.get(i))) {
                compostCount++;
            }
            if (landfill.contains(tagNames.get(i))) {
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

}
