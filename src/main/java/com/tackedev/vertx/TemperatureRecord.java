package com.tackedev.vertx;

public class TemperatureRecord {
    private String id;
    private double temperature;
    private long time;

    public TemperatureRecord() {
    }

    public TemperatureRecord(String id, double temperature, long time) {
        this.id = id;
        this.temperature = temperature;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
