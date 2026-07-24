package com.adam.ecolens.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "scan_history")
public class ScanHistoryEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String username;
    private String category;
    private float confidence;
    private String imageUri;
    private long timestamp;

    public ScanHistoryEntity(long id, String username, String category, float confidence, String imageUri, long timestamp) {
        this.id = id;
        this.username = username;
        this.category = category;
        this.confidence = confidence;
        this.imageUri = imageUri;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public float getConfidence() { return confidence; }
    public void setConfidence(float confidence) { this.confidence = confidence; }

    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
