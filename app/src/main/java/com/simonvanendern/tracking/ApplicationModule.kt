package com.simonvanendern.tracking

import android.content.Context
import androidx.room.Room
import com.google.gson.GsonBuilder
import com.simonvanendern.tracking.aggregation.RequestExecuter
import com.simonvanendern.tracking.communication.WebService
import com.simonvanendern.tracking.database.TrackingDatabase
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
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create()
        return Retrofit.Builder()
            .baseUrl("https://privacy-research-proud-platypus.eu-gb.mybluemix.net/")
//            .baseUrl("http://localhost:8888/")
            .addConverterFactory(GsonConverterFactory.create(gson))
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
    fun provideRequestRepository(
        webService: WebService,
        db: TrackingDatabase
    ): RequestRepository {
        return RequestRepository(db, webService)
    }

    @Singleton
    @Provides
    fun provideRequestExecutor(db: TrackingDatabase): RequestExecuter {
        return RequestExecuter(db)
    }

    @Singleton
    @Provides
    fun provideActivityRepository(db: TrackingDatabase): ActivityRepository {
        return ActivityRepository(db)
    }
}
