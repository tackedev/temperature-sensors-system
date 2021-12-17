package com.tackedev.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;

public class ListenerVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(ListenerVerticle.class);
    private static final DecimalFormat formater = new DecimalFormat("#.##");

    @Override
    public void start() {
        EventBus eventBus = vertx.eventBus();
        eventBus.<JsonObject> consumer("sensor.updates", message -> {
           JsonObject payload = message.body();
           String id = payload.getString("id");
           String temperature = formater.format(payload.getDouble("temperature"));

           logger.info("{} reports a temperature ~{}C", id, temperature);
        });
    }
}
