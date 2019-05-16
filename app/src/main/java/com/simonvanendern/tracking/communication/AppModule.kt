package com.simonvanendern.tracking.communication

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class AppModule {

    @Singleton
    @Provides
    fun provideWebservice() : Webservice {
        return Retrofit.Builder()
            .baseUrl("http://localhost:8888/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Webservice::class.java)
    }
}
