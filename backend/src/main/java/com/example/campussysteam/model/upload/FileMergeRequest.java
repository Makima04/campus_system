package com.example.campussysteam.model.upload;

import lombok.Data;

@Data
public class FileMergeRequest {
    private String fileMd5;
    private String fileName;
    private long fileSize;
    private int chunkCount;
} 