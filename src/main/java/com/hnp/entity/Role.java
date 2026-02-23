package com.hnp.entity;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonProperty;

@MongoEntity(collection = "roles")
public class Role extends PanacheMongoEntity {

    @BsonProperty("role_name")
    public String roleName;
}
