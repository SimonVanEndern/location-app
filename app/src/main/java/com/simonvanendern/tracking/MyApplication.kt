package com.simonvanendern.tracking

import android.app.Application

/**
 * Application class handling dependency injection on startup and
 * providing the application context to the injection module.
 */
class MyApplication : Application() {

    private val component: ApplicationComponent by lazy {
        DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        component.inject(this)
    }
}