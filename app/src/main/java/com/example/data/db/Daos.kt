package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AutoApprovalRuleDao {
    @Query("SELECT * FROM auto_approval_rules ORDER BY priority ASC, id ASC")
    fun getAllRules(): Flow<List<AutoApprovalRuleEntity>>

    @Query("SELECT * FROM auto_approval_rules WHERE isEnabled = 1 ORDER BY priority ASC, id ASC")
    suspend fun getActiveRulesSync(): List<AutoApprovalRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: AutoApprovalRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<AutoApprovalRuleEntity>)

    @Update
    suspend fun updateRule(rule: AutoApprovalRuleEntity)

    @Delete
    suspend fun deleteRule(rule: AutoApprovalRuleEntity)

    @Query("DELETE FROM auto_approval_rules WHERE id = :ruleId")
    suspend fun deleteRuleById(ruleId: Long)

    @Query("UPDATE auto_approval_rules SET isEnabled = :isEnabled WHERE id = :ruleId")
    suspend fun toggleRule(ruleId: Long, isEnabled: Boolean)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getRecentLogs(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    suspend fun getRecentLogsSync(): List<AuditLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity): Long

    @Query("DELETE FROM audit_logs")
    suspend fun clearLogs()

    @Query("DELETE FROM audit_logs WHERE id NOT IN (SELECT id FROM audit_logs ORDER BY timestamp DESC LIMIT 300)")
    suspend fun pruneOldLogs()
}

@Dao
interface RouterConfigDao {
    @Query("SELECT * FROM router_config WHERE id = 1 LIMIT 1")
    fun getConfig(): Flow<RouterConfigEntity?>

    @Query("SELECT * FROM router_config WHERE id = 1 LIMIT 1")
    suspend fun getConfigSync(): RouterConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: RouterConfigEntity)

    @Query("UPDATE router_config SET isAutomationActive = :isActive WHERE id = 1")
    suspend fun setAutomationActive(isActive: Boolean)
}

@Dao
interface BlockedClientDao {
    @Query("SELECT * FROM blocked_clients ORDER BY blockedAt DESC")
    fun getAllBlocked(): Flow<List<BlockedClientEntity>>

    @Query("SELECT * FROM blocked_clients")
    suspend fun getAllBlockedSync(): List<BlockedClientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun blockClient(client: BlockedClientEntity)

    @Query("DELETE FROM blocked_clients WHERE mac = :mac")
    suspend fun unblockClient(mac: String)

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_clients WHERE mac = :mac)")
    suspend fun isBlocked(mac: String): Boolean
}
