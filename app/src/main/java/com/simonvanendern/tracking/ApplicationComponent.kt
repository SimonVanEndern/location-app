package com.simonvanendern.tracking

import com.simonvanendern.tracking.repository.RequestRepository
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun inject(application: MyApplication)

//    fun requestRepository() : RequestRepository

//    fun inject(mainActivity: MainActivity)
}
