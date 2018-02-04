package com.example.ivan.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 200;
    private TextView iconTextView;
    private TextView cityTextView;
    private TextView detailsTextView;
    private TextView currentTempTextView;
    private AlertDialog.Builder alert;
    private Toast errorToast;
    private Retrofit retrofit;
    private WeatherAPIClient service;
    private LocationManager locationManager = null;

    private int sunset;
    private int sunrise;
    private ImageView backgroundImageView;

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
        getLongLat();
    }

    private void updateWeatherData(String city) {
        new Thread(() -> infoGetterCity(city)).start();
    }


    @SuppressLint("SetTextI18n")
    private void renderWeather(Response<WeatherResponse> response) {
        cityTextView = findViewById(R.id.city_text_view);
        cityTextView.setText(response.body().getName() + ", " + response.body().getOtherInfo().getCountry());

        backgroundImageView = findViewById(R.id.background_image_view);
        backgroundSetter(response);

        detailsTextView = findViewById(R.id.description_text_view);
        detailsTextView.setText(response.body().getWeather().get(0).getWeatherInfoMain());

        currentTempTextView = findViewById(R.id.temp_text_view);
        currentTempTextView.setText(String.valueOf((int) response.body().getMainTemp().getTemp()) + "°C");

        iconTextView = findViewById(R.id.icon_text_view);
        iconSetter(response);
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

    private void backgroundSetter(Response<WeatherResponse> response) {
        switch (response.body().getWeather().get(0).getWeatherInfoMain().toLowerCase()) {
            case "clouds":
            case "mist":
            case "smoke":
                backgroundImageView.setImageResource(R.drawable.clouds);
                break;

            case "thunderstorm":
            case "rain":
                backgroundImageView.setImageResource(R.drawable.rain);
                break;

            case "clear":
                backgroundImageView.setImageResource(R.drawable.sunny);
                break;

            case "snow":
                backgroundImageView.setImageResource(R.drawable.snow);
        }
    }

    private void iconSetter(Response<WeatherResponse> response) {
        if (response.body().getWeather().get(0).getWeatherInfoMain().equalsIgnoreCase("clouds") && response.body().getCloudsClass().getClouds() > 10) {
            iconTextView.setText(R.string.sunny_clouds);
        } else if (response.body().getWeather().get(0).getWeatherInfoMain().equalsIgnoreCase("Rain") || response.body().getWeather().get(0).getWeatherInfoMain().equalsIgnoreCase("Thunderstorm")) {
            iconTextView.setText(R.string.rainy);
        } else if (response.body().getWeather().get(0).getDescription().equalsIgnoreCase("light rain")) {
            iconTextView.setText(R.string.rain_light);
        } else if (response.body().getWeather().get(0).getWeatherInfoMain().equalsIgnoreCase("Snow")) {
            iconTextView.setText(R.string.snowy);
        } else if (response.body().getWeather().get(0).getWeatherInfoMain().equalsIgnoreCase("Clear")) {
            iconTextView.setText(R.string.sun_clear);
        }
    }

    private void getLongLat() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            double latitude = loc.getLatitude();
            double longitude = loc.getLongitude();

            infoGetterLongLat(latitude, longitude);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void infoGetterCity(String city) {

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        retrofit = new Retrofit.Builder()
                .baseUrl("http://api.openweathermap.org")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(WeatherAPIClient.class);
        Call<WeatherResponse> call = service.getWeatherByName(city);
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.body() == null) {
                    errorToast = Toast.makeText(getApplicationContext(), "Wrong city", Toast.LENGTH_SHORT);
                    errorToast.show();
                } else {
                    renderWeather(response);
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                System.out.println(t.getLocalizedMessage());
            }
        });
    }

    private void infoGetterLongLat(double latitude, double longitude) {

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        retrofit = new Retrofit.Builder()
                .baseUrl("http://api.openweathermap.org")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(WeatherAPIClient.class);
        Call<WeatherResponse> callLatLong = service.getWeatherByPosition((float) latitude, (float) longitude);
        callLatLong.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response == null) {
                    errorToast = new Toast(getApplicationContext());
                    errorToast.setText("Wrong city");
                    errorToast.setDuration(Toast.LENGTH_SHORT);
                    errorToast.show();
                } else {
                    renderWeather(response);
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                System.out.println(t.getLocalizedMessage());
            }
        });
    }
}

