package com.porejemplo.nube.server.auth.repository;

import com.porejemplo.nube.server.auth.entity.User;

import java.util.HashMap;
import java.util.Map;

public class UserIdentityMap {

    private static volatile UserIdentityMap instance;

    private Map<String, User> userMap = new HashMap<>();

    private UserIdentityMap() {
    }

    public static UserIdentityMap getInstance() {
        if (instance == null) {
            synchronized (UserIdentityMap.class) {
                if (instance == null) {
                    instance = new UserIdentityMap();
                }
            }
        }
        return instance;
    }

    public void addUser(User user) {
        this.userMap.put(user.getUsername(), user);
    }

    public User getUser(String username) {
        return this.userMap.get(username);
    }
}
