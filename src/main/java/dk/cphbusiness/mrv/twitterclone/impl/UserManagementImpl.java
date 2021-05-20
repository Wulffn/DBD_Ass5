package dk.cphbusiness.mrv.twitterclone.impl;

import dk.cphbusiness.mrv.twitterclone.contract.UserManagement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.cphbusiness.mrv.twitterclone.dto.*;
import dk.cphbusiness.mrv.twitterclone.util.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.Set;

public class UserManagementImpl implements UserManagement {

    private Jedis jedis;

    public UserManagementImpl(Jedis jedis) {
        this.jedis = jedis;
    }

    @Override
    public boolean createUser(UserCreation userCreation) {
        if (jedis.sismember("users", userCreation.username)) return false;
        try (Transaction t = jedis.multi()) {
            t.sadd("users", userCreation.username);
            t.hset("user:" + userCreation.username, Map.of(
                    "firstname", userCreation.firstname,
                    "lastname", userCreation.lastname,
                    "passwordHash", userCreation.passwordHash,
                    "birthday", userCreation.birthday,
                    "followers", "0",
                    "following", "0"
            ));
            t.exec();
        }
        return true;
    }

    @Override
    public UserOverview getUserOverview(String username) {
        if (!jedis.sismember("users", username)) return null;
        List<String> u = jedis.hmget("user:" + username, "firstname", "lastname", "followers", "following");
        UserOverview user = new UserOverview();
        user.firstname = u.get(0);
        user.lastname = u.get(1);
        user.numFollowers = Integer.valueOf(u.get(2));
        user.numFollowing = Integer.valueOf(u.get(3));
        return user;
    }

    @Override
    public boolean updateUser(UserUpdate userUpdate) {
        if (!jedis.sismember("users", userUpdate.username)) return false;
        Map<String, String> user = new HashMap<>();
        if (userUpdate.firstname != null) user.put("firstname", userUpdate.firstname);
        if (userUpdate.lastname != null) user.put("lastname", userUpdate.lastname);
        if (userUpdate.birthday != null) user.put("birthday", userUpdate.birthday);
        jedis.hset("user:" + userUpdate.username, user);
        return true;
    }

    @Override
    public boolean followUser(String username, String usernameToFollow) {
        if (!jedis.sismember("users", username) || !jedis.sismember("users", usernameToFollow)) return false;
        try(Transaction t = jedis.multi()) {
            t.sadd(username + ":follow", usernameToFollow);
            t.hincrBy("user:"+username, "following", 1);
            t.sadd(usernameToFollow + ":followed", username);
            t.hincrBy("user:"+usernameToFollow, "followers", 1);
            t.exec();
        }
        return true;
    }

    @Override
    public boolean unfollowUser(String username, String usernameToUnfollow) {
        if (!jedis.sismember("users", username) || !jedis.sismember("users", usernameToUnfollow)) return false;
        try(Transaction t = jedis.multi()) {
            t.srem(username+":follow", usernameToUnfollow);
            t.hincrBy("user:"+username, "following", -1);
            t.srem(usernameToUnfollow + ":followed", username);
            t.hincrBy("user:"+usernameToUnfollow, "followers", -1);
            t.exec();
        }
        return true;
    }

    @Override
    public Set<String> getFollowedUsers(String username) {
        if (!jedis.sismember("users", username)) return null;
        return jedis.smembers(username+":follow");
    }

    @Override
    public Set<String> getUsersFollowing(String username) {
        if (!jedis.sismember("users", username)) return null;
        return jedis.smembers(username+":followed");
    }

}
