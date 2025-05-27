package com.example.campussysteam.common.util;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CacheContext<T> {
    private T data;
    private boolean cacheHit;
    private long processingTime;
    private LocalDateTime expirationTime;
    private String cacheStatus;

    public static <T> CacheContext<T> of(T data, boolean cacheHit, long processingTime, LocalDateTime expirationTime) {
        CacheContext<T> context = new CacheContext<>();
        context.setData(data);
        context.setCacheHit(cacheHit);
        context.setProcessingTime(processingTime);
        context.setExpirationTime(expirationTime);
        context.setCacheStatus(cacheHit ? "HIT" : "MISS");
        return context;
    }

    public void setCacheStatus(String status) {
        this.cacheStatus = status;
    }
} 