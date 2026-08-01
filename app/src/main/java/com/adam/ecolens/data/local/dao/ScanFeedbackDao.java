package com.adam.ecolens.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import com.adam.ecolens.data.local.entity.ScanFeedbackEntity;

@Dao
public interface ScanFeedbackDao {
    @Insert
    long insertFeedback(ScanFeedbackEntity feedback);
}
