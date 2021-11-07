package com.learningbybuilding.supportportal.caching;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CachingWithoutLoader {
    @Test
    public void caching_get_if_not_present() throws ExecutionException {
        Cache<String, Integer> cache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .build();
        System.out.println(cache.get("User1", () -> 0));
        System.out.println(cache.getIfPresent("User1"));
    }
}
