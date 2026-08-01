package com.adam.ecolens.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "scan_feedback")
public class ScanFeedbackEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String predictedCategory;
    private String correctCategory;
    private String note;
    private String imageUri;
    private long timestamp;

    public ScanFeedbackEntity(long id, String predictedCategory, String correctCategory, String note, String imageUri, long timestamp) {
        this.id = id;
        this.predictedCategory = predictedCategory;
        this.correctCategory = correctCategory;
        this.note = note;
        this.imageUri = imageUri;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getPredictedCategory() { return predictedCategory; }
    public void setPredictedCategory(String predictedCategory) { this.predictedCategory = predictedCategory; }

    public String getCorrectCategory() { return correctCategory; }
    public void setCorrectCategory(String correctCategory) { this.correctCategory = correctCategory; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
