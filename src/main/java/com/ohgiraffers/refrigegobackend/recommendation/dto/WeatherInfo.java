package com.ohgiraffers.refrigegobackend.recommendation.dto;

public class WeatherInfo {

    private String conditionText;  // 예: "Partly cloudy"
    private double temperature;    // 예: 25.3

    public WeatherInfo() {}

    public WeatherInfo(String conditionText, double temperature) {
        this.conditionText = conditionText;
        this.temperature = temperature;
    }

    public String getConditionText() {
        return conditionText;
    }

    public void setConditionText(String conditionText) {
        this.conditionText = conditionText;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    @Override
    public String toString() {
        return "WeatherInfo{" +
                "conditionText='" + conditionText + '\'' +
                ", temperature=" + temperature +
                '}';
    }
}

