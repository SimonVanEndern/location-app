package com.simonvanendern.tracking.repository


import android.location.Location
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.simonvanendern.tracking.database.TrackingDatabase
import com.simonvanendern.tracking.database.data_model.aggregated.Trajectory
import com.simonvanendern.tracking.database.data_model.aggregated.TrajectoryDao
import com.simonvanendern.tracking.database.data_model.raw.GPSData
import com.simonvanendern.tracking.database.data_model.raw.GPSDataDao
import com.simonvanendern.tracking.database.data_model.raw.GPSLocation
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository handling the DAOs corresponding to GPS location
 */
@Singleton
class GPSRepository @Inject constructor(db: TrackingDatabase) {
    private val gpsLocationDao = db.gPSLocationDao()
    private val gpsDataDao = db.gPSDataDao()
    private val trajectoryDao = db.trajectoryDao()

    val recentLocations: LiveData<List<GPSData>> = gpsDataDao.get10MostRecentLocationTimestamps()

    // You must call this on a non-UI thread or your app will crash. So we're making this a
    // suspend function so the caller methods know this.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(gpsData: GPSData) {
        gpsDataDao.insert(gpsData)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(gpsLocation: GPSLocation): Long {
        return gpsLocationDao.insert(gpsLocation)
    }

    fun getAllGPSDataEntries(): List<GPSDataDao.GPSLocationWithTime> {
        return gpsDataDao.getAll()
    }

    fun getAllTrajectories(): List<TrajectoryDao.TrajectoryData> {
        return trajectoryDao.getAllFormatted()
    }

    @WorkerThread
    fun insertLocations(locations: List<Location>) {
        GlobalScope.launch {
            locations.forEach {
                val location = GPSLocation(
                    0,
                    it.longitude.toFloat(),
                    it.latitude.toFloat(),
                    it.speed
                )

                val id = gpsLocationDao.insert(location)

                gpsDataDao.insert(
                    GPSData(
                        id,
                        it.time,
                        false
                    )
                )
            }
        }
    }

    // Should be moved to a better place.
    // The Repository should only deal with data flow but not logic.
    /**
     * Computes Trajectories from raw GPS data that has not been processed yet.
     * The algorithm is as follows:
     *  1. All GPS data points are treated as one possible trajectory
     *  2. If there is no GPS data point for 10 minutes, the sequence is separated into
     *     two possible trajectories
     *  3. If there is no significant movement (regarding distance) for 2 minutes, the
     *     sequence is also separated into two possible trajectories
     *  4. The resulting sequences are the computed trajectories
     */
    fun aggregateGPSRoutes() {
        val raw = gpsDataDao.getAll()
        val parts = separateByTimeDifference(raw)
        val results = arrayListOf<Pair<GPSDataDao.GPSLocationWithTime, GPSDataDao.GPSLocationWithTime>>()
        parts.forEach {
            val stillMoments = findStillMoments(it)
            if (stillMoments.isNotEmpty()) {
                val stillIndices = combineStillMoments(stillMoments)
                results.addAll(computeRealTrajectories(it, stillIndices))
            }
        }

        gpsDataDao.setProcessed(results.maxBy { it.second.timestamp }?.second?.timestamp ?: 0)

        results.forEach {
            trajectoryDao.insert(
                Trajectory(
                    0,
                    it.first.timestamp,
                    it.second.timestamp,
                    it.first.id,
                    it.second.id,
                    0
                )
            )
        }
    }

    /**
     * Splits a sequence of GPS data points into a list of sequences
     * whenever two subsequent points were registered more than 10 minutes apart from each other.
     */
    private fun separateByTimeDifference(values: List<GPSDataDao.GPSLocationWithTime>): List<List<GPSDataDao.GPSLocationWithTime>> {
        val splitIndices = arrayListOf<Int>()
        var last: GPSDataDao.GPSLocationWithTime? = null

        for ((index, ele) in values.iterator().withIndex()) {
            if (last != null) {
                if (ele.timestamp - last.timestamp > 1000 * 60 * 10) {
                    splitIndices.add(index)
                }
            }
            last = ele
        }

        val parts = arrayListOf<List<GPSDataDao.GPSLocationWithTime>>()

        var lastIndex = 0
        splitIndices.forEach {
            parts.add(values.subList(lastIndex, it))
            lastIndex = it
        }
        if (splitIndices.size != 0) {
            parts.add(values.subList(lastIndex, values.size - 1))
        }

        return if (parts.isEmpty()) {
            arrayListOf(values)
        } else {
            parts
        }
    }

    /**
     * Computes a list of pairs of GPS points of no movement (regarding distance)
     * for at least 2 minutes
     */
    private fun findStillMoments(values: List<GPSDataDao.GPSLocationWithTime>): List<Pair<Int, Int>> {
        val noMovements = arrayListOf<Pair<Int, Int>>()
        var minIndex = 0

        for ((index, ele) in values.iterator().withIndex()) {
            for (i in minIndex until values.size) {
                val second = values[i]
                if (second.timestamp - ele.timestamp < 1000 * 120) {
                    continue
                } else {
                    if (distanceBetweenCoordinates(
                            second.latitude,
                            second.longitude,
                            ele.latitude,
                            ele.longitude
                        ) / ((second.timestamp - ele.timestamp) / 1000) < 0.6
                    ) {
                        noMovements.add(Pair(index, i))
                    }
                    minIndex = i
                    break
                }
            }
        }
        return noMovements
    }

    /**
     * Recursively fuses a list of pairs of GPS points to a smaller list of pairs of GPS points
     * by combining those that overlap in time.
     * E.g.: Pair1 = {start: 16:00, end: 16:03}
     *       Pair2 = {start: 16:02, end: 16:05}
     * Will result in the Pair {start: 16:00, end: 16:05}
     */
    private fun combineStillMoments(values: List<Pair<Int, Int>>): List<Pair<Int, Int>> {
        if (values.size == 1) {
            return values
        }
        val realNoMovements = arrayListOf<Pair<Int, Int>>()
        var current: Pair<Int, Int>? = null
        for ((index, ele) in values.iterator().withIndex()) {
            if (current == null) {
                current = ele
            } else {
                if (ele.first < current.second && ele.first >= current.first && ele.second >= current.second) {
                    current = Pair(current.first, ele.second)
                } else {
                    realNoMovements.add(current)
                    current = ele
                }
                if (index == values.size - 1) {
                    realNoMovements.add(current)
                }
            }
        }
        return realNoMovements
    }

    /**
     * @param points A sequence of GPS data points
     * @param stillIndices the indices of the points where the sequence should be cut
     * into separate trajectories
     * @return a list of trajectories consisting of start and end GPS point
     */
    private fun computeRealTrajectories(
        points: List<GPSDataDao.GPSLocationWithTime>,
        stillIndices: List<Pair<Int, Int>>
    ): List<Pair<GPSDataDao.GPSLocationWithTime, GPSDataDao.GPSLocationWithTime>> {
        val realMovements = arrayListOf<Pair<GPSDataDao.GPSLocationWithTime, GPSDataDao.GPSLocationWithTime>>()
        val first = 0
        val last = points.size - 1

        for ((index, ele) in stillIndices.iterator().withIndex()) {
            if (index == 0) {
                val pair = Pair(points[first], points[ele.first])
                if (distanceBetweenCoordinates(
                        pair.first.latitude,
                        pair.first.longitude,
                        pair.second.latitude,
                        pair.second.longitude
                    ) > 100
                ) {
                    realMovements.add(pair)
                }
            } else {

                val pair = Pair(points[stillIndices[index - 1].second], points[ele.first])
                if (distanceBetweenCoordinates(
                        pair.first.latitude,
                        pair.first.longitude,
                        pair.second.latitude,
                        pair.second.longitude
                    ) > 100
                ) {
                    realMovements.add(pair)
                }
            }
        }
        val pair = Pair(points[stillIndices[stillIndices.size - 1].second], points[last])
        if (distanceBetweenCoordinates(
                pair.first.latitude,
                pair.first.longitude,
                pair.second.latitude,
                pair.second.longitude
            ) > 100
        ) {
            realMovements.add(pair)
        }
        return realMovements
    }

    /**
     * Computes the distance between two GPS coordinates
     * defined by latitude and longitude
     */
    private fun distanceBetweenCoordinates(lat1: Float, lon1: Float, lat2: Float, lon2: Float): Double {
        //Taken from:
        //https://stackoverflow.com/questions/365826/calculate-distance-between-2-gps-coordinates
        val earthRadius = 6371000

        val dLat = degreesToRadians(lat2 - lat1)
        val dLon = degreesToRadians(lon2 - lon1)

        val lat1R = degreesToRadians(lat1)
        val lat2R = degreesToRadians(lat2)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1R) * Math.cos(lat2R)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    private fun degreesToRadians(degrees: Float): Double {
        return degrees * Math.PI / 180
    }
}