package dk.cphbusiness.mrv.twitterclone.impl;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.util.Timeout;
import dk.cphbusiness.mrv.twitterclone.contract.PostManagement;
import dk.cphbusiness.mrv.twitterclone.util.*;
import dk.cphbusiness.mrv.twitterclone.dto.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PostManagementImpl implements PostManagement {
    private Jedis jedis;
    private Time time;

    public PostManagementImpl(Jedis jedis, Time time) {
        this.jedis = jedis;
        this.time = time;
    }

    @Override
    public boolean createPost(String username, String message) {
        if(!jedis.sismember("users",username)) return false;
        jedis.zadd(username+":post", time.getCurrentTimeMillis(), message);
        return true;
    }

    @Override
    public List<Post> getPosts(String username) {
        Set<Tuple> posts = jedis.zrangeWithScores(username+":post",0,-1);
        return posts.stream().map(p -> new Post((long) p.getScore(), p.getElement())).collect(Collectors.toList());
    }

    @Override
    public List<Post> getPostsBetween(String username, long timeFrom, long timeTo) {
        return jedis.zrangeWithScores(username+":post",0,-1).stream().filter((po) -> po.getScore() >= timeFrom && po.getScore() <= timeTo).map(p -> {
            return new Post((long) p.getScore(), p.getElement());
        }).collect(Collectors.toList());
    }
}
