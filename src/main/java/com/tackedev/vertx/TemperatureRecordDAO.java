package com.tackedev.vertx;

import io.vertx.core.Future;

public interface TemperatureRecordDAO {

    Future<Integer> save(TemperatureRecord record);

    Future<Double> getAverageTemperature();

}
