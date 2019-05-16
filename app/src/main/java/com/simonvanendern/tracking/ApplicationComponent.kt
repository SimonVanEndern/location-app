package com.simonvanendern.tracking

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun inject(application: MyApplication)
//    fun inject(mainActivity: MainActivity)
}
