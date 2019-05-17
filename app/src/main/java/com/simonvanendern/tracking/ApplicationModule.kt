package com.simonvanendern.tracking

import android.app.Application
import androidx.room.Room
import com.simonvanendern.tracking.communication.WebService
import com.simonvanendern.tracking.database.TrackingDatabase
import com.simonvanendern.tracking.database.schemata.AggregationRequestDao
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class ApplicationModule(private val application: Application) {

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
    fun provideDb (application: Application) : TrackingDatabase {
        return Room.databaseBuilder(
            application,
            TrackingDatabase::class.java,
            "Location_database").build()
    }

    @Singleton
    @Provides
    fun provideAggregationRequestDao(db : TrackingDatabase) : AggregationRequestDao {
        return db.aggregationRequestDao()
    }
}
