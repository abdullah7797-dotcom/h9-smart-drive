package com.h9smartdrive.app.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val endedAt: Long?,
    val distanceKm: Double,
    val averageSpeedKph: Double?,
    val averageConsumption: Double?,
)

@Entity(tableName = "maintenance")
data class MaintenanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dueOdometerKm: Int?,
    val dueAt: Long?,
    val completed: Boolean = false,
)

@Dao
interface H9Dao {
    @Query("SELECT * FROM trips ORDER BY startedAt DESC")
    fun observeTrips(): Flow<List<TripEntity>>

    @Insert
    suspend fun insertTrip(trip: TripEntity)

    @Query("SELECT * FROM maintenance ORDER BY completed, dueAt")
    fun observeMaintenance(): Flow<List<MaintenanceEntity>>

    @Insert
    suspend fun insertMaintenance(item: MaintenanceEntity)
}

@Database(
    entities = [TripEntity::class, MaintenanceEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class H9Database : RoomDatabase() {
    abstract fun dao(): H9Dao
}
