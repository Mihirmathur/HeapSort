package com.example.melody.heapsortandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
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
import java.util.Collections;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ImageView thumbnail;
    Button clickme;
    TextView results;
    private VisionServiceClient client;
    Firebase firebase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        thumbnail = (ImageView) findViewById(R.id.thumbnail);
        clickme = (Button) findViewById(R.id.clickme);
        results = (TextView) findViewById(R.id.results);
        Firebase.setAndroidContext(this);
        firebase = new Firebase("https://heapsort-9a89b.firebaseio.com/");

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
            //TODO: Use Claifai API too
        }
    }

    private JSONArray getAnalysisResult(Bitmap b) throws VisionServiceException, IOException, JSONException {
        Gson gson = new Gson();
        String[] features = {"Tags"};
        String[] details = {};

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        AnalysisResult v =  this.client.analyzeImage(inputStream, features, details);
        System.out.println(gson.toJson(v));
        return (JSONArray) new JSONObject(gson.toJson(v)).get("tags");
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
            if(result == null){
                results.setText("An error occurred. Please try again");
            } else {
                clickme.setText(result.toString());
                trashCategories(result);
            }
        }
    }

    void trashCategories(final JSONArray categories) {

        final ArrayList<String> tagNames = new ArrayList<String>();

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

        firebase.child("categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Integer> counters = new HashMap<String, Integer>();
                for(DataSnapshot d: dataSnapshot.getChildren()){
                    System.out.println(d.toString());
                    int counter = 0;
                    for(String s: tagNames){
                        for(DataSnapshot childS: d.getChildren()){
                            if(s.equalsIgnoreCase((String) childS.getValue())){
                                    counter++;
                            }
                        }
                    }

                    counters.put(d.getKey(), counter);
                }

                String maxCategory = (String) dataSnapshot.child("default").getValue();
                int maxCount = 0;

                for(String key: counters.keySet()){
                    if(counters.get(key) > maxCount){
                        maxCategory = key;
                    }
                }

                //TODO add confirm button for adding to Firebase

                Firebase timestamp = firebase.child("history").push();
                timestamp.child("tags").setValue(tagNames);
                timestamp.child("category").setValue(maxCategory);
                results.setText("Please place your item in " + maxCategory);

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

}
