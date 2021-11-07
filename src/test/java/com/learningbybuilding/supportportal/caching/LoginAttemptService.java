package com.learningbybuilding.supportportal.caching;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {
    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final int ATTEMPT_INCREMENT = 1;
    public LoadingCache<String , Integer> loginAttemptCache;

    public LoginAttemptService() {
        loginAttemptCache = CacheBuilder.newBuilder()
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .maximumSize(100)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) throws Exception {
                        return 0;
                    }
                });
    }

    public void evictUserFromLoginAttemptCache(String userName) {
        loginAttemptCache.invalidate(userName);
    }

    public void addUserToLoginAttemptCache(String userName) throws ExecutionException {
        int attempts = loginAttemptCache.get(userName);
        loginAttemptCache.put(userName, attempts + ATTEMPT_INCREMENT);
    }

    public boolean hasExceededMaxAttempts(String userName) {
        return loginAttemptCache.getUnchecked(userName) >= MAX_FAILED_ATTEMPTS;
    }
}
