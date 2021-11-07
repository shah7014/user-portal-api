package com.learningbybuilding.supportportal.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class LoginAttemptCache {
    private LoadingCache<String, Integer> userToFailedAttempts;

    public LoginAttemptCache(int maxSize, int expirationTimeInMinutes) {
        this.userToFailedAttempts = CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expirationTimeInMinutes, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) throws Exception {
                        return 0;
                    }
                });
    }

    public void evictUserFromLoginAttemptCache(String username) {
        userToFailedAttempts.invalidate(username);
    }

    public void addUserToLoginAttemptCache(String userName) {
        try {
            int previousFailedAttempts = userToFailedAttempts.get(userName);
            userToFailedAttempts.put(userName, previousFailedAttempts + 1);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public boolean hasFailedAttemptsExceededMaxLimit(String userName) {
        try {
            return userToFailedAttempts.get(userName) >= 5;
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }
}
