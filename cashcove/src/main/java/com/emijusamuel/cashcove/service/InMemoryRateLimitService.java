package com.emijusamuel.cashcove.service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;


@Service
public class InMemoryRateLimitService implements RateLimitService{

     private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Value("${ratelimit.login.ip.capacity:50}")
    private long ipCapacity;

    @Value("${ratelimit.login.ip.refillSeconds:3600}")
    private long ipRefillSeconds;

    @Value("${ratelimit.login.user.capacity:5}")
    private long userCapacity;

    @Value("${ratelimit.login.user.refillSeconds:900}")
    private long userRefillSeconds;

    @Value("${ratelimit.isauth.user.capacity:10}")
    private long isAuthUserCapacity;

    @Value("${ratelimit.isauth.user.refillSeconds:600}")
    private long isAuthUserRefillSeconds;

    @Value("${ratelimit.isauth.ip.capacity:20}")
    private long isAuthIpCapacity;

    @Value("${ratelimit.isauth.ip.refillSeconds:1800}")
    private long isAuthIpRefillSeconds;

    @Value("${ratelimit.register.user.capacity:3}")
    private long registerUserCapacity;

    @Value("${ratelimit.register.user.refillSeconds:86400}")
    private long registerUserRefillSeconds;

    @Value("${ratelimit.register.ip.capacity:10}")
    private long registerIpCapacity;

    @Value("${ratelimit.register.ip.refillSeconds:3600}")
    private long registerIpRefillSeconds;

    @Value("${ratelimit.activate.ip.capacity:15}")
    private long activateIpCapacity;

    @Value("${ratelimit.activate.ip.refillSeconds:1800}")
    private long activateIpRefillSeconds;


private Bucket newBucket(long capacity, Duration refill) {
Refill refillStrategy = Refill.intervally(capacity, refill);
Bandwidth limit = Bandwidth.classic(capacity, refillStrategy);
return Bucket4j.builder().addLimit(limit).build();
}


public boolean tryConsume(String key, long tokens) {
    Bucket bucket = buckets.computeIfAbsent(key, this::createBucketForKey);
    return bucket.tryConsume(tokens);
}

private Bucket createBucketForKey(String key) {
    if (key.startsWith("ip:")) {
        return newBucket(ipCapacity, Duration.ofSeconds(ipRefillSeconds));
    } else if (key.startsWith("login:user:")) {
        return newBucket(userCapacity, Duration.ofSeconds(userRefillSeconds));
    } else if (key.startsWith("is-auth:user:")) {
        return newBucket(isAuthUserCapacity, Duration.ofSeconds(isAuthUserRefillSeconds));
    } else if (key.startsWith("is-auth:ip:")) {
        return newBucket(isAuthIpCapacity, Duration.ofSeconds(isAuthIpRefillSeconds));
    } else if (key.startsWith("register:user:")) {
        return newBucket(registerUserCapacity, Duration.ofSeconds(registerUserRefillSeconds));
    } else if (key.startsWith("register:ip:")) {
        return newBucket(registerIpCapacity, Duration.ofSeconds(registerIpRefillSeconds));
    } else if (key.startsWith("activate:ip:")) {
        return newBucket(activateIpCapacity, Duration.ofSeconds(activateIpRefillSeconds));
    } else {
        // Default for other keys
        return newBucket(5, Duration.ofMinutes(15));
    }
}


public long getNanosToRefill(String key) {
    Bucket b = buckets.get(key);
    if (b == null) return 0L;
    return b.estimateAbilityToConsume(1).getNanosToWaitForRefill();
}

}
