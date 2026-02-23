package com.hnp.entity;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.types.ObjectId;

import java.util.List;

@MongoEntity(collection = "users")
public class User extends PanacheMongoEntity {

    public String username;
    public String password;
    public String firstName;
    public String lastName;
    public String email;

    public List<ObjectId> roleIds;
}
