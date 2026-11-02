package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.RuleMatchType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AutoApprovalRuleEntity::class,
        AuditLogEntity::class,
        RouterConfigEntity::class,
        BlockedClientEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NdsDatabase : RoomDatabase() {
    abstract fun autoApprovalRuleDao(): AutoApprovalRuleDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun routerConfigDao(): RouterConfigDao
    abstract fun blockedClientDao(): BlockedClientDao

    companion object {
        @Volatile
        private var INSTANCE: NdsDatabase? = null

        fun getDatabase(context: Context): NdsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NdsDatabase::class.java,
                    "opennds_sentinel.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default configuration and rules
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getDatabase(context)
                                database.routerConfigDao().saveConfig(
                                    RouterConfigEntity(
                                        id = 1L,
                                        name = "OpenWrt AP",
                                        host = "192.168.1.1",
                                        port = 80,
                                        protocol = "BUILTIN_SIMULATOR",
                                        username = "root",
                                        password = "",
                                        ubusToken = "",
                                        cgiPath = "/cgi-bin/ndsctl",
                                        pollIntervalSeconds = 4,
                                        isAutomationActive = true,
                                        defaultSessionDurationMinutes = 60,
                                        autoDeauthOnExpire = true,
                                        autoDeauthOnQuota = true,
                                        autoApproveNewClients = true,
                                        gatewayName = "GuestLink Starlink Gateway"
                                    )
                                )

                                val defaultRules = listOf(
                                    AutoApprovalRuleEntity(
                                        name = "General Guest Auto-Pass",
                                        isEnabled = true,
                                        matchType = RuleMatchType.ALL_CLIENTS.name,
                                        matchPattern = "",
                                        grantDurationMinutes = 60,
                                        downloadRateLimitKbps = 10000,
                                        uploadRateLimitKbps = 3000,
                                        downloadQuotaMb = 1000,
                                        priority = 10,
                                        description = "Automatically grant standard 60-minute guest access with 10Mbps rate limit"
                                    ),
                                    AutoApprovalRuleEntity(
                                        name = "Staff / Trusted Mobile Devices",
                                        isEnabled = true,
                                        matchType = RuleMatchType.HOSTNAME_CONTAINS.name,
                                        matchPattern = "iPhone,Pixel,Galaxy,iPad",
                                        grantDurationMinutes = 240,
                                        downloadRateLimitKbps = 25000,
                                        uploadRateLimitKbps = 10000,
                                        downloadQuotaMb = 0,
                                        priority = 2,
                                        description = "Extended 4-hour pass with high-speed priority for recognized phone models"
                                    ),
                                    AutoApprovalRuleEntity(
                                        name = "Business Hours Automatic Access",
                                        isEnabled = false,
                                        matchType = RuleMatchType.SCHEDULE_WINDOW.name,
                                        matchPattern = "08:00-18:00",
                                        grantDurationMinutes = 120,
                                        downloadRateLimitKbps = 15000,
                                        uploadRateLimitKbps = 5000,
                                        downloadQuotaMb = 0,
                                        priority = 5,
                                        description = "Approve client requests only during daylight business hours (8 AM - 6 PM)"
                                    )
                                )
                                database.autoApprovalRuleDao().insertAll(defaultRules)

                                database.auditLogDao().insertLog(
                                    AuditLogEntity(
                                        timestamp = System.currentTimeMillis() - 300_000,
                                        mac = "SYSTEM",
                                        ip = "127.0.0.1",
                                        hostname = "GuestLink Core",
                                        action = "AUTO_APPROVED",
                                        durationMinutes = 0,
                                        ruleName = "Engine Initialized",
                                        details = "GuestLink engine initialized and monitoring"
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
