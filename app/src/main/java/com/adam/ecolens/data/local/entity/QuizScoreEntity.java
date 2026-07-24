package com.adam.ecolens.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quiz_scores")
public class QuizScoreEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String username;
    private int levelId;
    private int score;
    private int stars;
    private long completedAt;

    public QuizScoreEntity(long id, String username, int levelId, int score, int stars, long completedAt) {
        this.id = id;
        this.username = username;
        this.levelId = levelId;
        this.score = score;
        this.stars = stars;
        this.completedAt = completedAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getLevelId() { return levelId; }
    public void setLevelId(int levelId) { this.levelId = levelId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }

    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }
}
