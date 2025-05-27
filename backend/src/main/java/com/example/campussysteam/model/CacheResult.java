package com.example.campussysteam.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheResult<T> {
    private T data;
    private boolean cacheHit;
    private long processingTime;
    private LocalDateTime expirationTime;
} 