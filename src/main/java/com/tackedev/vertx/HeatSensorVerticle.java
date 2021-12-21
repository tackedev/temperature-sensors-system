package com.tackedev.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

import java.util.Random;
import java.util.UUID;

public class HeatSensorVerticle extends AbstractVerticle {
    private final String sensorId = UUID.randomUUID().toString();
    private double temperature = 21.0;
    private final Random random = new Random();

    @Override
    public void start() {
        vertx.setPeriodic(random.nextInt(5000) + 1000, this::updateTemperature);
    }

    private void updateTemperature(Long id) {
        temperature += delta() / 10;

        var payload = new JsonObject()
            .put("id", sensorId)
            .put("temperature", temperature)
            .put("time", System.currentTimeMillis());
        vertx.eventBus().publish("sensor.updates", payload);
    }

    private double delta() {
        if (random.nextInt() > 0) {
            return random.nextGaussian();
        } else {
            return -random.nextGaussian();
        }
    }
}
