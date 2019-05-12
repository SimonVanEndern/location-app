package tracking.database.schemata

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.ABORT
import androidx.room.Query

@Dao
interface GPSLocationDao {

    @Insert(onConflict = ABORT)
    fun insert(location: GPSLocation): Long

    @Query("SELECT * FROM gps_location_table WHERE id = :id LIMIT 1")
    fun getById(id: Long): GPSLocation

    @Query("""SELECT * FROM gps_location_table, gps_data_table
        WHERE id = location_id
        ORDER BY timestamp DESC
        LIMIT 10
    """)
    fun get10MostRecentLocations () : LiveData<List<GPSData>>
}