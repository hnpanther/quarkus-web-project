package com.hnp.service;


import com.hnp.security.AuthResource;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.ReactiveKeyCommands;
import io.quarkus.redis.datasource.value.ReactiveValueCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class RedisService {

    private static final Logger log = Logger.getLogger(RedisService.class.getName());

    private ReactiveKeyCommands<String> keyCommands;
    private ReactiveValueCommands<String, String> valueCommands;

    public RedisService(ReactiveRedisDataSource reactive) {
        log.log(Level.INFO, "Creating ReactiveKeyCommands");
        valueCommands = reactive.value(String.class);
        keyCommands = reactive.key();
    }

    public Uni<Void> set(String key, String value) {
        log.log(Level.INFO, "Start set key: " + key + " value: " + value);
        return valueCommands.set(key, value);
    }

    public Uni<Void> setEx(String key, String value, long ttlSeconds) {
        log.log(Level.INFO, "Start set key: " + key + " value: " + value + " ttlSeconds: " + ttlSeconds);
        return valueCommands.setex(key, ttlSeconds, value);
    }

    public Uni<String> get(String key) {
        log.log(Level.INFO, "Start get key: " + key);
        return valueCommands.get(key);
    }

    public Uni<Boolean> exists(String key) {
        log.log(Level.INFO, "Start check exists key: " + key);
        return keyCommands.exists(key);
    }

    public Uni<Void> delete(String key) {
        log.log(Level.INFO, "Start delete key: " + key);
        return keyCommands.del(key).replaceWithVoid();
    }




}
