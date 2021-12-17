package com.tackedev.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

public class SensorDataVerticle extends AbstractVerticle {

    private static final int DB_PORT = Integer.parseInt(System.getenv().getOrDefault("DB_PORT", "5432"));
    private static final String DB_HOST = System.getenv().getOrDefault("DB_HOST", "127.0.0.1");
    private static final String DB_NAME = System.getenv().getOrDefault("DB_NAME", "postgres");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", "admin");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "admin123");

    private static final int DB_POOL_MAX = Integer.parseInt(System.getenv().getOrDefault("DB_POOL_MAX", "8"));

    private SqlClient pool;

    @Override
    public void start() {
        pool = getPgPool();

        EventBus eventBus = vertx.eventBus();
        eventBus.consumer("sensor.updates", this::updateTemperature);
        eventBus.consumer("sensor.average", this::averageTemperature);
    }

    private void updateTemperature(Message<JsonObject> message) {
        JsonObject payload = message.body();
        String id = payload.getString("id");
        double temperature = payload.getDouble("temperature");
        long time = payload.getLong("time");

        String queryString = "INSERT INTO temperature_record (id, value, time) " +
            "VALUES ($1, $2, $3) " +
            "ON CONFLICT ON CONSTRAINT temperature_record_pk " +
            "DO UPDATE SET id=$1, value=$2, time=$3";

        pool.preparedQuery(queryString)
            .execute(Tuple.of(id, temperature, time));
    }

    private void averageTemperature(Message<JsonObject> message) {

        String queryString = "SELECT AVG(value) " +
            "FROM temperature_record";

        pool.preparedQuery(queryString)
            .execute(asyncResult -> {
                if (asyncResult.succeeded()) {
                    RowSet<Row> rows = asyncResult.result();
                    for (Row row : rows) {
                        double avgTemperature = row.getDouble(0);
                        JsonObject payload = new JsonObject().put("average", avgTemperature);
                        message.reply(payload);
                    }
                }
            });
    }

    private SqlClient getPgPool() {
        PgConnectOptions connectOptions = new PgConnectOptions()
            .setPort(DB_PORT)
            .setHost(DB_HOST)
            .setDatabase(DB_NAME)
            .setUser(DB_USER)
            .setPassword(DB_PASSWORD)
            .setCachePreparedStatements(true);

        PoolOptions poolOptions = new PoolOptions().setMaxSize(DB_POOL_MAX);

        return PgPool.client(vertx, connectOptions, poolOptions);
    }

}
