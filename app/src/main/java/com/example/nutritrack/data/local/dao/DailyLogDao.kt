package com.example.nutritrack.data.local.dao

import androidx.room.*
import com.example.nutritrack.data.local.entity.DailyLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_logs WHERE userId = :userId AND date = :date")
    fun getDailyLog(userId: String, date: String): Flow<DailyLogEntity?>

    @Query("SELECT * FROM daily_logs WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getDailyLogsInRange(userId: String, startDate: String, endDate: String): Flow<List<DailyLogEntity>>

    @Query("SELECT * FROM daily_logs WHERE userId = :userId ORDER BY date DESC LIMIT 7")
    fun getLastSevenDaysLogs(userId: String): Flow<List<DailyLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyLog(dailyLog: DailyLogEntity)

    @Update
    suspend fun updateDailyLog(dailyLog: DailyLogEntity)

    @Query("DELETE FROM daily_logs WHERE logId = :logId")
    suspend fun deleteDailyLog(logId: String)

    @Query("DELETE FROM daily_logs WHERE userId = :userId AND date < :beforeDate")
    suspend fun deleteOldLogs(userId: String, beforeDate: String)
}
