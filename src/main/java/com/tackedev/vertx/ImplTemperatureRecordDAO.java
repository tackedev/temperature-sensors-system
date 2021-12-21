package com.tackedev.vertx;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
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
    public void save(TemperatureRecord record, Handler<AsyncResult<RowSet<Row>>> handler) {
        String queryString = "INSERT INTO temperature_record (id, value, time) " +
            "VALUES ($1, $2, $3) " +
            "ON CONFLICT ON CONSTRAINT temperature_record_pk " +
            "DO UPDATE SET id=$1, value=$2, time=$3";

        pool.preparedQuery(queryString)
            .execute(
                Tuple.of(record.getId(), record.getTemperature(),
                record.getTime()), handler
            );
    }

    @Override
    public void getAverageTemperature(Handler<AsyncResult<RowSet<Row>>> handler) {
        String queryString = "SELECT AVG(value) " +
            "FROM temperature_record";

        pool.preparedQuery(queryString).execute(handler);
    }
}
