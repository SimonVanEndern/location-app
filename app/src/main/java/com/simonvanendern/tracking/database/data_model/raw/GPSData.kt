package com.simonvanendern.tracking.database.data_model.raw

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * This class saves GPS data together with the linked gps_location_table.
 * location_id corresponds to the id of the GPS data in gps_location table.
 * processed is a flag whether this entry has already been processed and used to
 * compute aggregated data in the aggregated package
 */
@Entity(
    tableName = "gps_data_table",
    foreignKeys = [ForeignKey(
        entity = GPSLocation::class,
        parentColumns = ["id"],
        childColumns = ["location_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class GPSData(
    @ColumnInfo(name = "location_id") val location_id: Long,
    @PrimaryKey @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "processed") val processed: Boolean
)