package com.adam.ecolens.data.local.dao;

import androidx.room.*;
import com.adam.ecolens.data.local.entity.UserEntity;
import kotlinx.coroutines.flow.Flow;
import java.util.List;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertUser(UserEntity user);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    UserEntity getUserByUsername(String username);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    Flow<UserEntity> observeUser(String username);

    @Query("UPDATE users SET totalPoints = totalPoints + :addPoints WHERE username = :username")
    void addPoints(String username, int addPoints);

    @Query("UPDATE users SET unlockedLevel = :newLevel WHERE username = :username AND unlockedLevel < :newLevel")
    void updateUnlockedLevel(String username, int newLevel);

    @Query("SELECT * FROM users ORDER BY totalPoints DESC LIMIT 10")
    Flow<List<UserEntity>> getTopUsers();
}
