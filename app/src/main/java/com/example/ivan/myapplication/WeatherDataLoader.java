package com.example.ivan.myapplication;

import android.content.Context;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ivan on 1/30/18.
 */

public class WeatherDataLoader {

    private static final String OPEN_WEATHER_MAP_API = "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric";
    private static final String KEY = "x-api-key";
    private static final String Response = "cod";
    private static final String NEW_LINE = "\n";
    private static final int ALL_GOOD = 200;

    static JSONObject getJSONData(Context context, String city) {
        try {
            URL url = new URL(String.format(OPEN_WEATHER_MAP_API, city));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty(KEY, context.getString(R.string.open_weather_map_api_key));

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder raw_data = new StringBuilder(1024);
            String tempData;
            while ((tempData = bufferedReader.readLine()) != null) {
                raw_data.append(tempData).append(NEW_LINE);
            }
            bufferedReader.close();

            JSONObject jsonObject = new JSONObject(raw_data.toString());

            if (jsonObject.getInt(Response) != ALL_GOOD) {
                return null;
            }
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
