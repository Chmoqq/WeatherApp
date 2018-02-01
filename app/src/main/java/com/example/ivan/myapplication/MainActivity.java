package com.example.ivan.myapplication;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    private Handler handler = new Handler();

    private TextView cityTextView;
    private TextView updatedTextView;
    private TextView detailsTextView;
    private TextView currentTempTextView;
    private AlertDialog.Builder alert;

    private int error;
    private String weather;
    private String city;
    private long date;
    private double temp;

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setError(int error) {
        this.error = error;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public void setCity(String city) {
        this.city = city;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(WeatherResponse event) {
        setError(event.getCod());
        setCity(event.getName());
        setWeather(event.getWeather().get(0).getMain());
        setTemp(event.getMainTemp().getTemp());
        setDate(event.getDt());
        renderWeather();
    }

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
        EventBus.getDefault().register(this);
    }

    private void updateWeatherData(String city) {
        new Thread(() -> WeatherDataLoader.infoGetter(city)).start();


    }

    @SuppressLint("SetTextI18n")
    private void renderWeather() {
        final String cityText = city;
        cityTextView = findViewById(R.id.text_view_1);
        cityTextView.setText(cityText);

        String detailsText = weather;
        detailsTextView = findViewById(R.id.text_view_2);
        detailsTextView.setText(detailsText);

        currentTempTextView = findViewById(R.id.text_view_3);
        currentTempTextView.setText(String.valueOf((int)temp) + "°C");

        updatedTextView = findViewById(R.id.text_view_4);
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String lastUpdate = dateFormat.format(new Date(this.date * 1000));
        updatedTextView.setText(lastUpdate);
    }


    private void dialogShow() {
        alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle("Change city");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        alert.setView(input);
        alert.setPositiveButton("Check", (dialogInterface, i) -> {
            MainActivity.this.updateWeatherData(input.getText().toString());

        });
        alert.show();
    }

}
