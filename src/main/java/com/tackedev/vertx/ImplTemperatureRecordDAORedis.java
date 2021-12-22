package com.tackedev.vertx;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImplTemperatureRecordDAORedis implements TemperatureRecordDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(TemperatureRecordDAO.class);

    private Redis client;

    public ImplTemperatureRecordDAORedis(Vertx vertx) {
        RedisOptions options = new RedisOptions();
        client = Redis.createClient(vertx, options);
    }

    @Override
    public Future<Integer> save(TemperatureRecord record) {
        JsonObject json = new JsonObject()
            .put("id", record.getId())
            .put("temperature", record.getTemperature())
            .put("time", record.getTime());

        Promise<Integer> promise = Promise.promise();
        client.send(Request.cmd(Command.SET).arg(record.getId()).arg(json.encode()),
            asyncResult -> {
                if (asyncResult.succeeded()) {
                    Response response = asyncResult.result();
                    promise.complete(response.size());
                } else {
                    promise.fail(asyncResult.cause());
                }
            });

        return promise.future();
    }

    @Override
    public Future<Double> getAverageTemperature() {
        Promise<Double> promise = Promise.promise();
        client.send(Request.cmd(Command.KEYS).arg("*"))
            .onSuccess(keyResponse -> {
                Request request = Request.cmd(Command.MGET);
                for (var item : keyResponse) {
                    request.arg(item.toString());
                }
                client.send(request, asyncResult -> {
                    if (asyncResult.succeeded()) {
                        Response valueResponse = asyncResult.result();
                        double averageTemperature = 0;
                        for (var item : valueResponse) {
                            JsonObject json = new JsonObject(item.toString());
                            averageTemperature += json.getDouble("temperature");
                        }
                        averageTemperature /= valueResponse.size();
                        promise.complete(averageTemperature);
                    } else {
                        promise.fail(asyncResult.cause());
                    }
                });
            })
            .onFailure(promise::fail);
        return promise.future();
    }
}
