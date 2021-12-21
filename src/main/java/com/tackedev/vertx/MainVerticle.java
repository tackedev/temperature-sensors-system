package com.tackedev.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        vertx.deployVerticle("com.tackedev.vertx.HeatSensorVerticle", new DeploymentOptions().setInstances(4));
        vertx.deployVerticle("com.tackedev.vertx.ListenerVerticle");
        vertx.deployVerticle("com.tackedev.vertx.HttpServerVerticle");
        vertx.deployVerticle("com.tackedev.vertx.SensorDataVerticle");
    }
}
