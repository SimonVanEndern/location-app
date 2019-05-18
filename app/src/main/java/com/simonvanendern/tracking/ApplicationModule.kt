package com.simonvanendern.tracking

import android.content.Context
import androidx.room.Room
import com.simonvanendern.tracking.aggregation.RequestExecuter
import com.simonvanendern.tracking.communication.WebService
import com.simonvanendern.tracking.database.TrackingDatabase
import com.simonvanendern.tracking.database.schemata.AggregationRequestDao
import com.simonvanendern.tracking.database.schemata.StepsDao
import com.simonvanendern.tracking.database.schemata.StepsRawDao
import com.simonvanendern.tracking.repository.ActivityRepository
import com.simonvanendern.tracking.repository.RequestRepository
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class ApplicationModule(private val application: Context) {

    @Singleton
    @Provides
    fun provideWebservice(): WebService {
        return Retrofit.Builder()
            .baseUrl("http://localhost:8888/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WebService::class.java)
    }

    @Provides
    @Singleton
    fun provideContext() = application

    @Provides
    @Singleton
    fun provideDb(application: Context): TrackingDatabase {
        return Room.databaseBuilder(
            application,
            TrackingDatabase::class.java,
            "Location_database"
        ).build()
    }

    @Singleton
    @Provides
    fun provideAggregationRequestDao(db: TrackingDatabase): AggregationRequestDao {
        return db.aggregationRequestDao()
    }

    @Singleton
    @Provides
    fun provideStepsDao(db: TrackingDatabase): StepsDao {
        return db.stepsDao()
    }

    @Singleton
    @Provides
    fun provideStepsRawDao(db: TrackingDatabase): StepsRawDao {
        return db.stepsRawDao()
    }

    @Singleton
    @Provides
    fun provideRequestRepository(
        webService: WebService,
        aggregationRequestDao: AggregationRequestDao
    ): RequestRepository {
        return RequestRepository(webService, aggregationRequestDao)
    }

    @Singleton
    @Provides
    fun provideRequestExecutor(db: TrackingDatabase): RequestExecuter {
        return RequestExecuter(db)
    }

    @Singleton
    @Provides
    fun provideActivityRepository(db: TrackingDatabase): ActivityRepository {
        return ActivityRepository(db.activityTransitionDao(), db.activityDao())
    }
}
