package com.emijusamuel.cashcove.service;

public interface RateLimitService {

    boolean tryConsume(String key, long tokens);
    long getNanosToRefill(String key);

}
