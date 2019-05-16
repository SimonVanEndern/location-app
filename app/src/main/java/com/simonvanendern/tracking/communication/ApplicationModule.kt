package com.simonvanendern.tracking.communication

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.simonvanendern.tracking.database.LocationRoomDatabase
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class ApplicationModule(private val application: Application) {

    @Singleton
    @Provides
    fun provideWebservice(): Webservice {
        return Retrofit.Builder()
            .baseUrl("http://localhost:8888/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Webservice::class.java)
    }

    @Provides
    @Singleton
    fun provideContext() = application

    @Provides
    @Singleton
    fun provideDb (application: Application) : LocationRoomDatabase {
        return Room.databaseBuilder(
            application,
            LocationRoomDatabase::class.java,
            "Location_database").build()
    }

//    @Singleton
//    @Provides
//    fun provideAggregationRequestDao() : AggregationRequestDao {
//        return LocationRoomDatabase.getDatabase()
//    }
}
