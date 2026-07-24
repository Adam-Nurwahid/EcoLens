package com.adam.ecolens.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.adam.ecolens.data.local.dao.QuizScoreDao;
import com.adam.ecolens.data.local.dao.ScanHistoryDao;
import com.adam.ecolens.data.local.dao.UserDao;
import com.adam.ecolens.data.local.entity.QuizScoreEntity;
import com.adam.ecolens.data.local.entity.ScanHistoryEntity;
import com.adam.ecolens.data.local.entity.UserEntity;

@Database(
    entities = {UserEntity.class, ScanHistoryEntity.class, QuizScoreEntity.class},
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract ScanHistoryDao scanHistoryDao();
    public abstract QuizScoreDao quizScoreDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "ecolens_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
