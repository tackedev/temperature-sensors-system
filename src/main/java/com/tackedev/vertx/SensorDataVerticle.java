package com.tackedev.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorDataVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(SensorDataVerticle.class);

    private TemperatureRecordDAO dao;

    @Override
    public void start() {
        dao = new ImplTemperatureRecordDAORedis(vertx);

        EventBus eventBus = vertx.eventBus();
        eventBus.consumer("sensor.updates", this::updateTemperature);
        eventBus.consumer("sensor.average", this::averageTemperature);
    }

    private void updateTemperature(Message<JsonObject> message) {
        var payload = message.body();

        String id = payload.getString("id");
        double temperature = payload.getDouble("temperature");
        long time = payload.getLong("time");

        var record = new TemperatureRecord(id, temperature, time);

        Future<Integer> future = dao.save(record);
        future
            .onSuccess(effectedRows -> LOGGER.info("{} saved", id))
            .onFailure(cause -> LOGGER.error("Saved temperature record fail caused by: {}", cause.toString()));
    }

    private void averageTemperature(Message<JsonObject> message) {
        Future<Double> future = dao.getAverageTemperature();
        future
            .onSuccess(avgTemperature -> {
                var payload = new JsonObject().put("average", avgTemperature);
                message.reply(payload);
            })
            .onFailure(cause -> LOGGER.error("Get average temperature fail caused by: {}", cause.toString()));
    }
}
