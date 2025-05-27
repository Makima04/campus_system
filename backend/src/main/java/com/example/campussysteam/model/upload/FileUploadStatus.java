package com.example.campussysteam.model.upload;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FileUploadStatus {
    private boolean uploaded = false;
    private String fileUrl;
    private List<Integer> uploadedChunks = new ArrayList<>();
} 