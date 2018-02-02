package com.example.ivan.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {


    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 200;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private TextView iconTextView;
    private TextView cityTextView;
    private TextView updatedTextView;
    private TextView detailsTextView;
    private TextView currentTempTextView;
    private AlertDialog.Builder alert;
    static Toast errorToast;

    private double latitude;
    private double longitude;
    private long date;
    private int sunset;
    private int sunrise;
    private int error;
    private String country;
    private int humidity;
    private int clouds;
    private double temp;
    private String weatherInfoMain;
    private String city;
    private String weatherDescription;
    private ImageView backgroundImageView;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(WeatherResponse event) {
        infoSetter(event);
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
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        EventBus.getDefault().register(this);
        getLongLat();
        WeatherDataLoader.infoGetterCity(null, (float) latitude, (float) longitude);
    }

    private void updateWeatherData(String city) {
        new Thread(() -> WeatherDataLoader.infoGetterCity(city, 0, 0)).start();
    }


    @SuppressLint("SetTextI18n")
    private void renderWeather() {
        cityTextView = findViewById(R.id.city_text_view);
        cityTextView.setText(city + ", " + country);

        backgroundImageView = findViewById(R.id.background_image_view);
        backgroundSetter();

        String detailsText = weatherInfoMain;
        detailsTextView = findViewById(R.id.description_text_view);
        detailsTextView.setText(detailsText);

        currentTempTextView = findViewById(R.id.temp_text_view);
        currentTempTextView.setText(String.valueOf((int) temp) + "°C");

        iconTextView = findViewById(R.id.icon_text_view);
        iconSetter();

        updatedTextView = findViewById(R.id.last_update_text_view);
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

    private void infoSetter(WeatherResponse dataEvent) {

        error = dataEvent.getCod();
        city = dataEvent.getName();
        temp = dataEvent.getMainTemp().getTemp();
        date = dataEvent.getDt();
        clouds = dataEvent.getCloudsClass().getClouds();
        sunrise = dataEvent.getOtherInfo().getSunrise();
        sunset = dataEvent.getOtherInfo().getSunset();
        humidity = dataEvent.getMainTemp().getHumidity();
        weatherInfoMain = dataEvent.getWeather().get(0).getWeatherInfoMain();
        weatherDescription = dataEvent.getWeather().get(0).getDescription();
        country = dataEvent.getOtherInfo().getCountry();
    }

    private void backgroundSetter() {
        if (weatherInfoMain.equalsIgnoreCase("Clouds") || weatherInfoMain.equalsIgnoreCase("Mist") || weatherInfoMain.equalsIgnoreCase("Smoke")) {
            backgroundImageView.setImageResource(R.drawable.clouds);
        } else if (weatherInfoMain.equalsIgnoreCase("Thunderstorm") || weatherInfoMain.equalsIgnoreCase("Rain")) {
            backgroundImageView.setImageResource(R.drawable.rain);
        } else if (weatherInfoMain.equalsIgnoreCase("Clear")) {
            backgroundImageView.setImageResource(R.drawable.sunny);
        }
    }

    private void iconSetter() {
        if (humidity > 5 && humidity < 50 || clouds > 1) {
            iconTextView.setText(R.string.sunny_clouds);
        } else if (weatherInfoMain.equalsIgnoreCase("Rain") || weatherInfoMain.equalsIgnoreCase("Thunderstorm")) {
            iconTextView.setText(R.string.rainy);
        } else if (weatherDescription.equalsIgnoreCase("light rain")) {
            iconTextView.setText(R.string.rain_light);
        } else if (weatherInfoMain.equalsIgnoreCase("Snow")) {
            iconTextView.setText(R.string.snowy);
        } else if (weatherInfoMain.equalsIgnoreCase("Clear")) {
            iconTextView.setText(R.string.sun_clear);
        }
    }

    private void getLongLat() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mFusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

}
