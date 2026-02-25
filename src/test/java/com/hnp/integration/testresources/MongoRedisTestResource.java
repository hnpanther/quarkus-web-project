package com.hnp.integration.testresources;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.GenericContainer;

import java.util.HashMap;
import java.util.Map;

public class MongoRedisTestResource implements QuarkusTestResourceLifecycleManager {

    private MongoDBContainer mongo;
    private GenericContainer<?> redis;

    @Override
    public Map<String, String> start() {
        Map<String, String> props = new HashMap<>();

        // Mongo
        mongo = new MongoDBContainer("mongo:8.2.6-rc0");
        mongo.start();
        props.put("quarkus.mongodb.connection-string", mongo.getReplicaSetUrl());

        // Redis
        redis = new GenericContainer<>("redis:8.2.1")
                .withExposedPorts(6379);
        redis.start();
        String redisHostPort = redis.getHost() + ":" + redis.getMappedPort(6379);
        props.put("quarkus.redis.hosts", "redis://" + redis.getHost() + ":" + redis.getMappedPort(6379));

        return props;
    }

    @Override
    public void stop() {
        if (mongo != null) {
            mongo.stop();
        }
        if (redis != null) {
            redis.stop();
        }
    }
}