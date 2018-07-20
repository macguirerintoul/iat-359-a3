package com.mrintoul.macguirerintoul_a3;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, View.OnDragListener {
    Button retrieveButton, clearButton; // buttons
    TextView[] textViews = new TextView[4]; // array of textviews for the 4 random numbers
    TextView[] multiples = new TextView[4]; // array of textviews for the 4 drop locations
    TextView multiplesOf2, multiplesOf3, multiplesOf5, multiplesOf10; // textviews of the drop locations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // add the number textviews to the array
        textViews[0] = findViewById(R.id.number0);
        textViews[1] = findViewById(R.id.number1);
        textViews[2] = findViewById(R.id.number2);
        textViews[3] = findViewById(R.id.number3);

        // initialize the textviews of the drop areas
        multiplesOf2 = findViewById(R.id.multiplesOf2);
        multiplesOf3 = findViewById(R.id.multiplesOf3);
        multiplesOf5 = findViewById(R.id.multiplesOf5);
        multiplesOf10 = findViewById(R.id.multiplesOf10);

        // add drop areas to the array
        multiples[0] = multiplesOf2;
        multiples[1] = multiplesOf3;
        multiples[2] = multiplesOf5;
        multiples[3] = multiplesOf10;

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

        // initialize and set click listeners for buttons
        retrieveButton = findViewById(R.id.retrieve);
        retrieveButton.setOnClickListener(this);
        clearButton = findViewById(R.id.clear);
        clearButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // check which button was pressed
        switch (v.getId()) {
            case R.id.retrieve:
                if (checkConnection()) {
                    clear(); // clear numbers and drop areas

                    // this is for testing when the API is down
                    for (int i = 0; i < 4; i++) {
                        textViews[i].setText(String.valueOf(i));
                    }

                    // retrieve 4 random numbers from API
                    //new RetrieveNumbersTask().execute("http://qrng.anu.edu.au/API/jsonI.php?length=4&type=uint8");
                }
                break;
            case R.id.clear:
                clear(); // clear numbers and drop areas
                break;
        }
    }

    // resets the textviews when clear button is clicked
    public void clear() {
        for (int i = 0; i < 4; i++) {
            textViews[i].setText("");
            textViews[i].setVisibility(View.VISIBLE);
            multiples[i].setText("");
        }
    }

    // check if the device is connected to the internet
    public boolean checkConnection() {
        ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;

        try {
            networkInfo = connectMgr.getActiveNetworkInfo(); // get network information if any
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        if(networkInfo != null && networkInfo.isConnected()) {
            // connected to internet
            return true;
        } else {
            // not connected to internet
            Toast.makeText(this, "no network connection", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    // called when the user has touched or started to drag a view
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            // the user has touched a view, get data to be dragged
            ClipData data = ClipData.newPlainText("","");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);

            // start dragging the item touched
            view.startDrag(data, shadowBuilder, view, 0);
            return true;
        } else {
            return false;
        }
    }

    // called when a drag is happening
    @Override
    public boolean onDrag(View v, DragEvent dragEvent) {
        switch (dragEvent.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                break;
            case DragEvent.ACTION_DROP:
                // handle the dragged view being dropped over a target view
                View view = (View) dragEvent.getLocalState();

                // view dragged item is being dropped on and the multiple of the drop area
                TextView dropTarget = (TextView) v;
                Integer dropTargetMultiple = Integer.valueOf((String) dropTarget.getContentDescription());

                // view being dropped and the value of the view being dropped
                TextView dropped = (TextView) view;
                Integer droppedValue = Integer.valueOf((String) dropped.getText());

                // boolean whether or not the dropped view is a multiple of the target view's value
                Boolean isMultiple = false;

                // check if the number being dragged is a multiple of the drop area's multiple
                if (droppedValue % dropTargetMultiple == 0) {
                    isMultiple = true;
                }

                if (isMultiple) {
                    // stop displaying the value where it was before it was dragged
                    view.setVisibility(View.INVISIBLE);

                    // update the text in the target view with the number being dropped
                    dropTarget.setText(dropTarget.getText() + " " + dropped.getText());

                    // let there be bold
                    dropTarget.setTypeface(Typeface.DEFAULT_BOLD);
                }
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                break;
            default:
                break;
        }
        return true;
    }

    // returns a string from JSON
    private String readJSONData(String myurl) throws IOException {
        InputStream is = null;
        int len = 2500;

        URL url = new URL(myurl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            conn.setReadTimeout(10000); // timeout of reading in ms
            conn.setConnectTimeout(15000); // timeout of connecting in ms
            conn.setRequestMethod("GET"); // request method
            conn.setDoInput(true); // say we're reading data from this connection
            conn.connect(); // make it happen
            int response = conn.getResponseCode(); // response code of request
            Log.d("tag", "The response is: " + response); // let us know
            is = conn.getInputStream(); // retrieve the inputstream
            return readIt(is, len); // inputstream to string via readIt
        } finally {
            if (is != null) {
                is.close(); // close the input stream
                conn.disconnect(); // close the connection
            }
        }
    }

    // converts inputstream to string
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    // asynctask to retrieve random numbers from a JSON api
    class RetrieveNumbersTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            try {
                return readJSONData(urls[0]); // get the stringified JSON data from the url passed in
            } catch(IOException e){
                // didn't work
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result); // initialize json object
                JSONArray numberData = jsonObject.getJSONArray("data"); // initialize json array of the data
                String toastText = ""; // the string to be displayed in the toast

                // set each textview as the numbers in data
                for (int i = 0; i < numberData.length(); i++) {
                    textViews[i].setText(String.valueOf(numberData.get(i)));
                    toastText += " " + String.valueOf(numberData.get(i));
                }

                //display the numbers in a toast
                Toast.makeText(getBaseContext(), toastText, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                // didn't work
                Log.d("exception", "onPostExecute");
                e.printStackTrace();
            }
        }
    }
}
