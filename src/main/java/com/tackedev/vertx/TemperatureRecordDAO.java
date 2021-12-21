package com.tackedev.vertx;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

public interface TemperatureRecordDAO {

    static TemperatureRecordDAO getInstance(Vertx vertx) {
        return new ImplTemperatureRecordDAO(vertx);
    }

    void save(TemperatureRecord record, Handler<AsyncResult<RowSet<Row>>> handler);

    void getAverageTemperature(Handler<AsyncResult<RowSet<Row>>> handler);

}
