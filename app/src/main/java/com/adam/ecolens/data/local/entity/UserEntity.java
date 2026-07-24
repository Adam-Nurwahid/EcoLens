package com.adam.ecolens.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey
    @NonNull
    private String username;
    private String password;
    private String fullName;
    private int totalPoints;
    private int unlockedLevel;

    public UserEntity(@NonNull String username, String password, String fullName, int totalPoints, int unlockedLevel) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.totalPoints = totalPoints;
        this.unlockedLevel = unlockedLevel;
    }

    @NonNull
    public String getUsername() { return username; }
    public void setUsername(@NonNull String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public int getUnlockedLevel() { return unlockedLevel; }
    public void setUnlockedLevel(int unlockedLevel) { this.unlockedLevel = unlockedLevel; }
}
