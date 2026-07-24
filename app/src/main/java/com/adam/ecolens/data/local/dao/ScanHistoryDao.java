package com.adam.ecolens.data.local.dao;

import androidx.room.*;
import com.adam.ecolens.data.local.entity.ScanHistoryEntity;
import kotlinx.coroutines.flow.Flow;
import java.util.List;

@Dao
public interface ScanHistoryDao {
    @Insert
    long insertScan(ScanHistoryEntity scan);

    @Query("SELECT * FROM scan_history WHERE username = :username ORDER BY timestamp DESC")
    Flow<List<ScanHistoryEntity>> getHistoryByUsername(String username);

    @Query("SELECT category, COUNT(*) as count FROM scan_history WHERE username = :username GROUP BY category")
    Flow<List<CategoryCount>> getCategoryCountsByUsername(String username);

    @Query("SELECT COUNT(*) FROM scan_history WHERE username = :username")
    Flow<Integer> getTotalScansByUsername(String username);
}
