package com.tackedev.vertx;

import io.vertx.core.AbstractVerticle;
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
        dao = TemperatureRecordDAO.getInstance(vertx);

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

        dao.save(record, asyncResult -> {
            if (asyncResult.succeeded()) {
                LOGGER.info("{} saved", id);
            } else {
                LOGGER.error("Saving {} fail :(", id);
            }
        });
    }

    private void averageTemperature(Message<JsonObject> message) {
        dao.getAverageTemperature(asyncResult -> {
            if (asyncResult.succeeded()) {
                var rowSet = asyncResult.result();
                for (var row : rowSet) {
                    double avgTemperature = row.getDouble(0);
                    var payload = new JsonObject().put("average", avgTemperature);
                    message.reply(payload);
                }
            } else {
                LOGGER.error("Get average fail!");
            }
        });
    }
}
