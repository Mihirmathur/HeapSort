package com.example.melody.heapsortandroid;

import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;
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
import java.util.List;

public class HeapSort extends AppCompatActivity {

    ImageView thumbnail;
    Button clickme;
    TextView results;
    TextView tags;
    private VisionServiceClient client;
    Firebase firebase;
    AlertDialog.Builder confirm;
    private ClarifaiClient clarifai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        thumbnail = (ImageView) findViewById(R.id.thumbnail);
        clickme = (Button) findViewById(R.id.clickme);
        tags = (TextView) findViewById(R.id.tags);
        results = (TextView) findViewById(R.id.results);
        Firebase.setAndroidContext(this);
        firebase = new Firebase("https://heapsort-9a89b.firebaseio.com/");
        confirm = new AlertDialog.Builder(this);

        setSupportActionBar(toolbar);
        if (client==null){
            client = new VisionServiceRestClient(getString(R.string.subscription_key));
        }
        clarifai = new ClarifaiClient(getString(R.string.client_id), getString(R.string.client_secret));

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

    private ArrayList<String> getAnalysisResult(Bitmap b) throws VisionServiceException, IOException, JSONException {
        Gson gson = new Gson();
        String[] features = {"Tags"};
        String[] details = {};

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        AnalysisResult v =  this.client.analyzeImage(inputStream, features, details);
        List<RecognitionResult> cResults = clarifai.recognize(new RecognitionRequest(output.toByteArray()));

        ArrayList<String> tagNames = new ArrayList<String>();
        JSONArray microsoft = (JSONArray) new JSONObject(gson.toJson(v)).get("tags");

        //put tag names in tagNames
        for (int i = 0; i < microsoft.length(); i++) {
            try {
                System.out.println(microsoft.getJSONObject(i));
                tagNames.add(microsoft.getJSONObject(i).getString("name"));
                System.out.println(tagNames.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (Tag t: cResults.get(0).getTags()){
            tagNames.add(t.getName());
        }

        return tagNames;
    }

    private class ComputerVision extends AsyncTask<Bitmap, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Bitmap... params) {
            try {
                return getAnalysisResult(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            if(result == null){
                results.setText("An error occurred. Please try again");
            } else {
                trashCategories(result);
            }
        }
    }

    void trashCategories(final ArrayList<String> tagNames) {

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

                final String finalMaxCategory = maxCategory;

                confirm.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which ){
                        Firebase timestamp = firebase.child("history").push();
                        timestamp.child("tags").setValue(tagNames);
                        timestamp.child("category").setValue(finalMaxCategory);
                    }
                });
                confirm.setNegativeButton("NO", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){

                    }
                });
                confirm.setMessage("Send results to Firebase?");
//                confirm.create().show();
                Firebase timestamp = firebase.child("history").push();
                timestamp.child("tags").setValue(tagNames);
                timestamp.child("category").setValue(finalMaxCategory);

                String tagsToPrint ="";
                for(String i: tagNames){
                    tagsToPrint += i + " ";
                }
                tags.setText(tagsToPrint);
                results.setText("Please place your item in " + maxCategory);
                if (maxCategory.equals("recycle")){
                    clickme.setBackgroundResource(R.drawable.click_button_recycle);
                }
                if (maxCategory.equals("landfill")){
                    clickme.setBackgroundResource(R.drawable.click_button_landfill);
                }
                if (maxCategory.equals("compost")) {
                    clickme.setBackgroundResource(R.drawable.click_button_compost);
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

}
