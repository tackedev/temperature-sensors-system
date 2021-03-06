package com.tackedev.vertx;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

public class ImplTemperatureRecordDAO implements TemperatureRecordDAO {

    private static final int DB_PORT = Integer.parseInt(System.getenv().getOrDefault("DB_PORT", "5432"));
    private static final String DB_HOST = System.getenv().getOrDefault("DB_HOST", "127.0.0.1");
    private static final String DB_NAME = System.getenv().getOrDefault("DB_NAME", "postgres");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", "admin");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "admin123");

    private static final int DB_POOL_MAX = Integer.parseInt(System.getenv().getOrDefault("DB_POOL_MAX", "8"));

    private final SqlClient pool;

    public ImplTemperatureRecordDAO(Vertx vertx) {

        var connectOptions = new PgConnectOptions()
            .setPort(DB_PORT)
            .setHost(DB_HOST)
            .setDatabase(DB_NAME)
            .setUser(DB_USER)
            .setPassword(DB_PASSWORD)
            .setCachePreparedStatements(true);

        var poolOptions = new PoolOptions().setMaxSize(DB_POOL_MAX);

        pool = PgPool.client(vertx, connectOptions, poolOptions);
    }

    @Override
    public Future<Integer> save(TemperatureRecord record) {
        String queryString = "INSERT INTO temperature_record (id, value, time) " +
            "VALUES ($1, $2, $3) " +
            "ON CONFLICT ON CONSTRAINT temperature_record_pk " +
            "DO UPDATE SET id=$1, value=$2, time=$3";

        Promise<Integer> promise = Promise.promise();
        pool.preparedQuery(queryString)
            .execute(
                Tuple.of(record.getId(), record.getTemperature(), record.getTime()),
                asyncResult -> {
                    if (asyncResult.succeeded()) {
                        var rowSet = asyncResult.result();
                        promise.complete(rowSet.rowCount());
                    } else {
                        promise.fail(asyncResult.cause());
                    }
                }
            );
        return promise.future();
    }

    @Override
    public Future<Double> getAverageTemperature() {
        String queryString = "SELECT AVG(value) " +
            "FROM temperature_record";

        Promise<Double> promise = Promise.promise();
        pool.preparedQuery(queryString).execute(asyncResult -> {
            if (asyncResult.succeeded()) {
                var rowSet = asyncResult.result();
                for (var row : rowSet) {
                    double avgTemperature = row.getDouble(0);
                    promise.complete(avgTemperature);
                }
            } else {
                promise.fail(asyncResult.cause());
            }
        });
        return promise.future();
    }
}
