package com.tackedev.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitMQConsumerVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQConsumerVerticle.class);

    private static final String RABBITMQ_USER = System.getenv().getOrDefault("RABBITMQ_USER", "guest");
    private static final String RABBITMQ_PASSWORD = System.getenv().getOrDefault("RABBITMQ_PASSWORD", "guest");
    private static final String RABBITMQ_HOST = System.getenv().getOrDefault("RABBITMQ_HOST", "localhost");
    private static final int RABBITMQ_PORT = Integer.parseInt(System.getenv().getOrDefault("RABBITMQ_PORT", "5672"));
    private static final String RABBITMQ_VHOST = System.getenv().getOrDefault("RABBITMQ_VHOST", "/");

    @Override
    public void start() {
        createRabbitMQClient().onSuccess(this::producerHandler);

        createRabbitMQClient().onSuccess(this::consumerHandler);
    }

    private void producerHandler(RabbitMQClient client) {
        vertx.setPeriodic(2000, time -> {
            client.basicPublish("temperature.average", "average", Buffer.buffer(), publishResult -> {
                if (publishResult.succeeded()) {
                    LOGGER.info("Sent message to Exchange!");
                } else {
                    LOGGER.error("Fail to send message: {}", publishResult.cause().getMessage());
                }
            });
        });
    }

    private void consumerHandler(RabbitMQClient client) {
        client.basicConsumer("average", consumerAsyncResult -> {
            RabbitMQConsumer consumer = consumerAsyncResult.result();
            consumer.handler(message -> {
                vertx.eventBus().<JsonObject>request("sensor.average", "", asyncResult -> {
                    if (asyncResult.succeeded()) {
                        double avgTemp = asyncResult.result().body().getDouble("average");
                        LOGGER.info("Average Temperature: {}", avgTemp);
                    }
                });
            });
        });
    }

    private Future<RabbitMQClient> createRabbitMQClient() {
        RabbitMQOptions options = new RabbitMQOptions()
            .setUser(RABBITMQ_USER)
            .setPassword(RABBITMQ_PASSWORD)
            .setHost(RABBITMQ_HOST)
            .setPort(RABBITMQ_PORT)
            .setVirtualHost(RABBITMQ_VHOST)
            .setAutomaticRecoveryEnabled(true)
            .setReconnectAttempts(Integer.MAX_VALUE)
            .setReconnectInterval(500);

        RabbitMQClient client = RabbitMQClient.create(vertx, config());

        Promise<RabbitMQClient> promise = Promise.promise();
        client.start(asyncResult -> {
            if (asyncResult.succeeded()) {
                LOGGER.info("RabbitMQ successfully connected!");
                promise.complete(client);
            } else {
                LOGGER.info("Fail to connect RabbitMQ: {}", asyncResult.cause().getMessage());
                promise.fail(asyncResult.cause());
            }
        });

        return promise.future();
    }
}
