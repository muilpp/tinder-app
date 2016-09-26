package com.tinderapp.model.api_data;

import java.util.List;

public class Photo {
    private List<ProcessedFiles> processedFiles;

    public List<ProcessedFiles> getProcessedFiles() {
        return processedFiles;
    }
    public void setProcessedFiles(List<ProcessedFiles> processedFiles) {
        this.processedFiles = processedFiles;
    }
}