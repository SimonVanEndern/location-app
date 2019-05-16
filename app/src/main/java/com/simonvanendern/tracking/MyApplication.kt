package com.simonvanendern.tracking

import android.app.Application


class MyApplication : Application() {

    val component: ApplicationComponent by lazy {
        DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()
    }



    override fun onCreate() {
        super.onCreate()
        component.inject(this)
    }

//    fun component(): ApplicationComponent? {
//        return component
//    }
}