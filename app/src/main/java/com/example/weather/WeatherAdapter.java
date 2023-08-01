package com.example.weather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.model.Weather;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Weather> weatherArrayList;

    public WeatherAdapter(Context context, ArrayList<Weather> weatherArrayList) {
        this.context = context;
        this.weatherArrayList = weatherArrayList;
    }

    @NonNull
    @Override
    public WeatherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.weather_recyclerview_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.ViewHolder holder, int position) {

        Weather weather = weatherArrayList.get(position);
        holder.temperature.setText(weather.getTemperature() + " °c");
        Picasso.get().load("http:".concat(weather.getIcon())).into(holder.weathercondition);
        holder.windspeed.setText(weather.getWindSpeed() + " Km/h");

        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat output = new SimpleDateFormat("hh:mm aa");
        try {
            Date date = input.parse(weather.getTime());
            holder.time.setText(output.format(date));
        }
        catch(ParseException e){
            Log.d("Ujwal", e.toString());
        }

    }

    @Override
    public int getItemCount() {
        return weatherArrayList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView temperature, time, windspeed;
        private ImageView weathercondition;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);
            temperature = itemView.findViewById(R.id.temperature);
            time = itemView.findViewById(R.id.time);
            windspeed = itemView.findViewById(R.id.windspeed);
            weathercondition = itemView.findViewById(R.id.weatherconditionicon);

        }
    }
}
