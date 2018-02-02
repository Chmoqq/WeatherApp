package com.example.ivan.myapplication;

import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class WeatherDataLoader {

    static void infoGetterCity(String city, float latitude, float longitude) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.openweathermap.org")
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        WeatherAPIClient service = retrofit.create(WeatherAPIClient.class);

        if (city != null) {

            Call<WeatherResponse> call = service.getWeatherByName(city);
            call.enqueue(new Callback<WeatherResponse>() {
                @Override
                public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                    if (response.body() != null) {
                        EventBus.getDefault().post(response.body());
                    } else {
                        MainActivity.errorToast.setText("Wrong city");
                        MainActivity.errorToast.setDuration(Toast.LENGTH_SHORT);
                        MainActivity.errorToast.show();
                    }

                }

                @Override
                public void onFailure(Call<WeatherResponse> call, Throwable t) {
                    System.out.println(t.getLocalizedMessage());
                }
            });
        } else {

            Call<WeatherResponse> callLatLong = service.getWeatherByPosition(latitude, longitude);
            callLatLong.enqueue(new Callback<WeatherResponse>() {
                @Override
                public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {

                    if (response.body() != null) {
                        EventBus.getDefault().post(response.body());
                    } else {
                        MainActivity.errorToast.setText("Wrong city");
                        MainActivity.errorToast.setDuration(Toast.LENGTH_SHORT);
                        MainActivity.errorToast.show();
                    }
                }

                @Override
                public void onFailure(Call<WeatherResponse> call, Throwable t) {
                }
            });
        }

    }

}
