package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    TextView textView;
    TextView textTemp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.btn_submit);
        final EditText editText = findViewById(R.id.edit_city);
        imageView = findViewById(R.id.img_weather);
        textView = findViewById(R.id.TV_city);
        textTemp = findViewById(R.id.TV_temp);

        // 배경 설정
        ConstraintLayout constraintLayout = findViewById(R.id.layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();
        // 배경 설정

        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String city = editText.getText().toString();
                String content = "https://api.openweathermap.org/data/2.5/weather?q="+city+"&appid=ddbc49d9fe249b781ff7ba92d94105b8";
                callWeatherData(content);
                textView.setText(city);
            }
        });

    }

    static class Weather extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            try{
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream streamIn = connection.getInputStream();
                InputStreamReader streamReader = new InputStreamReader(streamIn);

                int data = streamReader.read(); // 한 기호 읽어서 미리 처리 (데이터가 들어왔는지, 아닌지)
                StringBuilder weatherContent = new StringBuilder(); // String data + String data or char data + char data

                while(data != -1){
                    char ch = (char) data;
                    weatherContent.append(ch);
                    data = streamReader.read();
                }

                return weatherContent.toString();

            }catch(MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void callWeatherData(String content){
        Weather weather = new Weather();
        try {
            String data = weather.execute(content).get(); // Async Task
            JSONObject jsonObject = new JSONObject(data);
            JSONArray jsonArray = new JSONArray(jsonObject.getString("weather"));
            JSONObject jsonObject2 = new JSONObject(jsonObject.getString("main"));

            String icon = "";
            for(int i=0; i<jsonArray.length(); i++){
                JSONObject subJsonObject = jsonArray.getJSONObject(i);
                icon = subJsonObject.getString("icon");
            }
            setIcon(icon);

            double temp = 0.0;
            temp = jsonObject2.getDouble("temp");
            setTemp(temp);
        }
        catch (NullPointerException e){
            Toast toast = Toast.makeText(this.getApplicationContext(), "바른 도시명을 입력해주세요",Toast.LENGTH_SHORT);
            toast.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void setIcon(String icon){
        String target = "http://openweathermap.org/img/wn/" + icon + "@2x.png";
        Uri uri = Uri.parse(target);
        Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(imageView);
    }
    private void setTemp(Double temp){
        temp = temp -  273.15;
        temp = Double.parseDouble(String.format("%.2f", temp));
        String tempstr = Double.toString(temp)+"ºC";
        textTemp.setText(tempstr);
    }

    private void setWeatherImg(String main, String description){
        if(main.equals("Clear")){
            imageView.setImageResource(R.drawable.icon_clear_sky);
        }else if(main.equals("Clouds") && description.equals("few clouds")){
            imageView.setImageResource(R.drawable.icon_few_clouds);
        }else if(main.equals("Clouds") && description.equals("scattered clouds")){
            imageView.setImageResource(R.drawable.icon_scattered_clouds);
        }else if(main.equals("Clouds") && description.equals("broken clouds")){
            imageView.setImageResource(R.drawable.icon_broken_clouds);
        }else if(main.equals("Clouds") && description.equals("overcast clouds")){
            imageView.setImageResource(R.drawable.icon_broken_clouds);
        }else if(main.equals("Snow")){
            imageView.setImageResource(R.drawable.icon_snow);
        }else if(main.equals("Thunderstorm")){
            imageView.setImageResource(R.drawable.icon_thunderstorm);
        }else if(main.equals("Drizzle")){
            imageView.setImageResource(R.drawable.icon_shower_rain);
        }else if(main.equals("Rain")){
            if(description.equals("light rain")||description.equals("moderate rain")||description.equals("heavy intensity rain")||description.equals("very heavy rain")||description.equals("extreme rain"))
                imageView.setImageResource(R.drawable.icon_rain);
            else if(description.equals("freezing rain"))
                imageView.setImageResource(R.drawable.icon_snow);
            else if(description.equals("light intensity shower rain") || description.equals("shower rain")||description.equals("heavy intensity shower rain")||description.equals("ragged shower rain"))
                imageView.setImageResource(R.drawable.icon_shower_rain);
        }else
            imageView.setImageResource(R.drawable.icon_mist);
    }
}

