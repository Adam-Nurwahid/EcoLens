package com.adam.ecolens.data.local.dao;

import androidx.room.*;
import com.adam.ecolens.data.local.entity.QuizScoreEntity;
import kotlinx.coroutines.flow.Flow;
import java.util.List;

@Dao
public interface QuizScoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOrUpdateScore(QuizScoreEntity score);

    @Query("SELECT * FROM quiz_scores WHERE username = :username AND levelId = :levelId LIMIT 1")
    QuizScoreEntity getScoreByLevel(String username, int levelId);

    @Query("SELECT * FROM quiz_scores WHERE username = :username")
    Flow<List<QuizScoreEntity>> getAllScoresByUsername(String username);
}
