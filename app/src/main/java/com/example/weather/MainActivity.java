package com.example.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weather.databinding.ActivityMainBinding;
import com.example.weather.model.Weather;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding activityMainBinding;
    private ArrayList<Weather> weatherArrayList;
    private WeatherAdapter weatherAdapter;
    private final int PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // THIS WILL BE USED TO MAKE OUR APPLICATION FULL SCREEN
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        weatherArrayList = new ArrayList<Weather>();
        weatherAdapter = new WeatherAdapter(this, weatherArrayList);
        activityMainBinding.recyclerviewweather.setAdapter(weatherAdapter);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                &&
           ActivityCompat.checkSelfPermission(
                   this,
                   Manifest.permission.ACCESS_FINE_LOCATION
           ) != PackageManager.PERMISSION_GRANTED
        ){

            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    PERMISSION_CODE
            );

        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        String cityname = getCityName(location.getLatitude(), location.getLongitude());
        getWeatherInfo(cityname);

        activityMainBinding.search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = activityMainBinding.cityedittext.getText().toString();
                if(city.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter city name", Toast.LENGTH_SHORT).show();
                }
                else{
                    activityMainBinding.cityname.setText(city);
                    getWeatherInfo(city);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Please provide the permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }

    private String getCityName(double latitude, double longitude){

        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        String cityname = "Not found";
        try{
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 10);
            for(Address address : addressList){
                if(address != null){
                    String city = address.getLocality();
                    if(city != null && !city.equals("")){
                        cityname = city;
                    }
                    else{
                        Log.d("TAG", "CITY NOT FOUND");
                        Toast.makeText(this, "User City Not Found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return cityname;
    }

    private void getWeatherInfo(String cityname){

        String url = "http://api.weatherapi.com/v1/forecast.json?key=585fdbe1a8c145e69a992839230105&q=" + cityname + "&days=1&aqi=yes&alerts=yes";
        activityMainBinding.cityname.setText(cityname);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(JSONObject response) {
                activityMainBinding.loading.setVisibility(View.GONE);
                activityMainBinding.home.setVisibility(View.VISIBLE);
                weatherArrayList.clear();

                try {

                    JSONObject jsonObject = response.getJSONObject("current");
                    String temperature = jsonObject.getString("temp_c");
                    int isDay = jsonObject.getInt("is_day");

                    jsonObject = jsonObject.getJSONObject("condition");
                    String conditiontext = jsonObject.getString("text");
                    String conditionicon = jsonObject.getString("icon");

                    activityMainBinding.temperature.setText(temperature + " °c");
                    Picasso.get().load("http:".concat(conditionicon)).into(activityMainBinding.weatherconditionicon);
                    activityMainBinding.weatherconditiontext.setText(conditiontext);
                    if(isDay == 1){
                        Picasso.get().load("https://img.freepik.com/premium-vector/day-with-clouds-weather-app-screen-mobile-interface-design-forecast-weather-background-time-concept-vector-banner_87946-4137.jpg?w=2000").into(activityMainBinding.backgound);
                    }
                    else{
                        Picasso.get().load("https://img.freepik.com/premium-vector/night-with-clouds-weather-app-screen-mobile-interface-design-forecast-weather-background-time-concept-vector-banner_87946-4287.jpg?w=2000").into(activityMainBinding.backgound);
                    }

                    JSONObject forecastobject = response.getJSONObject("forecast");
                    JSONObject forecastobj = forecastobject.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourjsonarray = forecastobj.getJSONArray("hour");
                    for(int i=0; i<hourjsonarray.length(); i++){

                        JSONObject hourjsonobject = hourjsonarray.getJSONObject(i);
                        String time = hourjsonobject.getString("time");
                        String temp = hourjsonobject.getString("temp_c");
                        String weatherconditionimage = hourjsonobject.getJSONObject("condition").getString("icon");
                        String windspeed = hourjsonobject.getString("wind_kph");

                        weatherArrayList.add(new Weather(time, temp, weatherconditionimage, windspeed));

                    }

                    weatherAdapter.notifyDataSetChanged();

                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Please enter valid city name.", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);

    }

}