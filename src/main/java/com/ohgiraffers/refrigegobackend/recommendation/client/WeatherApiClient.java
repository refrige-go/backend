package com.ohgiraffers.refrigegobackend.recommendation.client;

import com.ohgiraffers.refrigegobackend.recommendation.dto.WeatherInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class WeatherApiClient {

    @Value("${weatherapi.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public WeatherInfo getWeather(double lat, double lon) {
        String location = lat + "," + lon;
        String url = "http://api.weatherapi.com/v1/current.json?key=" + apiKey + "&q=" + location;

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map body = response.getBody();
            if (body == null || !body.containsKey("current")) {
                return null;
            }

            Map current = (Map) body.get("current");

            Map conditionMap = (Map) current.get("condition");
            String conditionText = (String) conditionMap.get("text");
            double temperature = Double.parseDouble(current.get("temp_c").toString());

            return new WeatherInfo(conditionText, temperature);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
