package com.simonvanendern.tracking.communication

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun inject(application: MyApplication)
//    fun inject(mainActivity: MainActivity)
}
