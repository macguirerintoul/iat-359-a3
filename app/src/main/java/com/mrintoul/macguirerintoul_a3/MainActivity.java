package com.mrintoul.macguirerintoul_a3;

import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, View.OnDragListener {
    Button retrieveButton, clearButton; // buttons
    TextView[] textViews = new TextView[4]; // array of textviews for the 4 random numbers
    TextView multiplesOf2, multiplesOf3, multiplesOf5, multiplesOf10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViews[0] = findViewById(R.id.number0);
        textViews[1] = findViewById(R.id.number1);
        textViews[2] = findViewById(R.id.number2);
        textViews[3] = findViewById(R.id.number3);

        multiplesOf2 = findViewById(R.id.multiplesOf2);
        multiplesOf3 = findViewById(R.id.multiplesOf3);
        multiplesOf5 = findViewById(R.id.multiplesOf5);
        multiplesOf10 = findViewById(R.id.multiplesOf10);

        // set touch listeners
        textViews[0].setOnTouchListener(this);
        textViews[1].setOnTouchListener(this);
        textViews[2].setOnTouchListener(this);
        textViews[3].setOnTouchListener(this);

        // set drag listeners
        multiplesOf2.setOnDragListener(this);
        multiplesOf3.setOnDragListener(this);
        multiplesOf5.setOnDragListener(this);
        multiplesOf10.setOnDragListener(this);

        retrieveButton = findViewById(R.id.retrieve);
        retrieveButton.setOnClickListener(this);

        clearButton = findViewById(R.id.clear);
        clearButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.retrieve:
                new RetrieveNumbersTask().execute("http://qrng.anu.edu.au/API/jsonI.php?length=4&type=uint8");
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            //the user has touched the View to drag it
            //prepare the drag
            ClipData data = ClipData.newPlainText("","");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            //start dragging the item touched
            view.startDrag(data, shadowBuilder, view, 0);
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean onDrag(View v, DragEvent dragEvent) {
        switch (dragEvent.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                //no action necessary
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                //no action necessary
                break;
            case DragEvent.ACTION_DROP:
                //handle the dragged view being dropped over a target view
                View view = (View) dragEvent.getLocalState();

                //stop displaying the view where it was before it was dragged
                view.setVisibility(View.INVISIBLE);

                // view dragged item is being dropped on
                TextView dropTarget = (TextView) v;

                // view being dropped
                TextView dropped = (TextView) view;

                if (Integer.valueOf((String) dropped.getText()) % Integer.valueOf((String) dropTarget.getText()) == 0) {
                    // check to make sure the value being dropped is a multiple of the
                    //update the text in the target view to reflect the data being dropped
                    dropTarget.setText(dropTarget.getText() + " " + dropped.getText());

                    //make it bold to highlight the fact that an item has been dropped
                    dropTarget.setTypeface(Typeface.DEFAULT_BOLD);
                }


                break;
            case DragEvent.ACTION_DRAG_ENDED:
                //no action necessary
                break;
            default:
                break;
        }
        return true;
    }


    private String readJSONData(String myurl) throws IOException {
        InputStream is = null;
        int len = 2500;

        URL url = new URL(myurl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("tag", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
                conn.disconnect();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    class RetrieveNumbersTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            try {
                return readJSONData(urls[0]);
            } catch(IOException e){
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray numberData = jsonObject.getJSONArray("data");
                String toastText = "";

                // set each textview as the numbers
                for (int i = 0; i < numberData.length(); i++) {
                    textViews[i].setText(String.valueOf(numberData.get(i)));
                    toastText += " " + String.valueOf(numberData.get(i));
                }

                //display the numbers in a toast
                Toast.makeText(getBaseContext(), toastText, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.d("exception", "onPostExecute");
                e.printStackTrace();
            }
        }
    }
}
