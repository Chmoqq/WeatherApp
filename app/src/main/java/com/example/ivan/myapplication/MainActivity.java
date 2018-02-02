package com.example.ivan.myapplication;

import android.annotation.SuppressLint;
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


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {


    private TextView iconTextView;
    private TextView cityTextView;
    private TextView updatedTextView;
    private TextView detailsTextView;
    private TextView currentTempTextView;
    private AlertDialog.Builder alert;
    static Toast errorToast;

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

    public void setWeatherInfoMain(String weatherInfoMain) {
        this.weatherInfoMain = weatherInfoMain;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setWeatherDescription(String weatherDescription) {
        this.weatherDescription = weatherDescription;
    }

    public void setSunset(int sunset) {
        this.sunset = sunset;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public void setSunrise(int sunrise) {
        this.sunrise = sunrise;
    }

    public void setClouds(int clouds) {
        this.clouds = clouds;
    }

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
        this.weatherInfoMain = weather;
    }

    public void setCity(String city) {
        this.city = city;
    }


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
        EventBus.getDefault().register(this);
    }

    private void updateWeatherData(String city) {
        new Thread(() -> WeatherDataLoader.infoGetter(city)).start();


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

        setError(dataEvent.getCod());
        setCity(dataEvent.getName());
        setWeather(dataEvent.getWeather().get(0).getDescription());
        setTemp(dataEvent.getMainTemp().getTemp());
        setDate(dataEvent.getDt());
        setClouds(dataEvent.getCloudsClass().getClouds());
        setSunrise(dataEvent.getOtherInfo().getSunrise());
        setSunset(dataEvent.getOtherInfo().getSunset());
        setHumidity(dataEvent.getMainTemp().getHumidity());
        setWeatherInfoMain(dataEvent.getWeather().get(0).getWeatherInfoMain());
        setWeatherDescription(dataEvent.getWeather().get(0).getDescription());
        setCountry(dataEvent.getOtherInfo().getCountry());
    }

    private void backgroundSetter() {
        if (weatherInfoMain.equalsIgnoreCase("Clouds") || weatherInfoMain.equalsIgnoreCase("Mist") || weatherInfoMain.equalsIgnoreCase("Smoke") ) {
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
        } else if(weatherDescription.equalsIgnoreCase("light rain")) {
            iconTextView.setText(R.string.rain_light);
        } else if (weatherInfoMain.equalsIgnoreCase("Snow")) {
            iconTextView.setText(R.string.snowy);
        } else  if (weatherInfoMain.equalsIgnoreCase("Clear")) {
            iconTextView.setText(R.string.sun_clear);
        }
    }

}
