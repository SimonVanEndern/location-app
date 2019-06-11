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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject

/**
 * Abstracted Repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */
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

    //    @Suppress("RedundantSuspendModifier")
//    @WorkerThread
    fun getAll(): List<GPSDataDao.GPSLocationWithTime> {
        return gpsDataDao.getAll()
    }

    fun getAllTrajectories () : List<TrajectoryDao.TrajectoryData>{
        return trajectoryDao.getAllFormatted()
    }

    @WorkerThread
    fun insertLocations(locations: List<Location>) {
        GlobalScope.launch {
            async(Dispatchers.IO) {
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
    }

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
//        val test =
//            results.joinToString("") { "<trk><trkseg><trkpt lat = \"${it.first.latitude}\" lon=\"${it.first.longitude}\"></trkpt><trkpt lat = \"${it.second.latitude}\" lon=\"${it.second.longitude}\"></trkpt></trkseg></trk>" }

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
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")

        val test2 = trajectoryDao.getTrajectories(
            dateFormat.parse("2019-05-20").time,
            dateFormat.parse("2019-05-30").time
        )

//        return results

    }

    fun computeRealTrajectories(
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

    fun combineStillMoments(values: List<Pair<Int, Int>>): List<Pair<Int, Int>> {
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

    fun findStillMoments(values: List<GPSDataDao.GPSLocationWithTime>): List<Pair<Int, Int>> {
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

    fun separateByTimeDifference(values: List<GPSDataDao.GPSLocationWithTime>): List<List<GPSDataDao.GPSLocationWithTime>> {
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

        if (parts.isEmpty()) {
            return arrayListOf(values)
        } else {
            return parts
        }
    }

    fun degreesToRadians(degrees: Float): Double {
        return degrees * Math.PI / 180
    }

    fun distanceBetweenCoordinates(lat1: Float, lon1: Float, lat2: Float, lon2: Float): Double {
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
}