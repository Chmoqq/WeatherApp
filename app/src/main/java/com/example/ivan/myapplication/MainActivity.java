package com.example.ivan.myapplication;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    private Handler handler = new Handler();

    private Typeface weatherFont;
    private TextView cityTextView;
    private TextView updatedTextView;
    private TextView detailsTextView;
    private TextView currentTempTextView;
    private TextView weatherIcon;
    private AlertDialog.Builder alert;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
     dialogShow();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void updateWeatherData(String city) {
        new Thread() {
            public void run() {
                final JSONObject json = WeatherDataLoader.getJSONData(MainActivity.this.getApplicationContext(), city);

                if (json == null) {
                    handler.post(() -> Toast.makeText(MainActivity.this.getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show());
                } else {
                    handler.post(() -> {
                        MainActivity.this.renderWeather(json);
                    });
                }
            }
        }.start();

    }

    @SuppressLint("SetTextI18n")
    private void renderWeather(JSONObject json) {
        try {
            final String cityText = json.getString("name").toUpperCase(Locale.ENGLISH) + ". " + json.getJSONObject("sys").getString("country");
            cityTextView = findViewById(R.id.text_view_1);
            cityTextView.setText(cityText);

            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            String detailsText = details.getString("description").toUpperCase(Locale.ENGLISH) + "\n" + "Humidity: "
                    + main.getString("humidity") + "%" + "\n"
                    + "Pressure: " + main.getString("pressure") + "hPa";

            detailsTextView = findViewById(R.id.text_view_2);
            detailsTextView.setText(detailsText);

            @SuppressLint("DefaultLocale") String currentTempText = String.format("%.2f", main.getDouble("temp")) + "C";
            currentTempTextView = findViewById(R.id.text_view_3);
            currentTempTextView.setText(currentTempText);

            DateFormat date = DateFormat.getDateTimeInstance();
            String updateOn = date.format(new Date(json.getLong("dt") * 1000));
            updatedTextView = findViewById(R.id.text_view_4);
            updatedTextView.setText("Last update: " + updateOn);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dialogShow() {
        alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle("Change city");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        alert.setView(input);
        alert.setPositiveButton("Check", (dialogInterface, i) -> updateWeatherData(input.getText().toString()));
        alert.show();
    }
}
