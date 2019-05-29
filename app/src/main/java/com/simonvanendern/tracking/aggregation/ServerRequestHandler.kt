package com.simonvanendern.tracking.aggregation

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simonvanendern.tracking.ApplicationModule
import com.simonvanendern.tracking.DaggerApplicationComponent
import com.simonvanendern.tracking.R
import com.simonvanendern.tracking.repository.RequestRepository
import javax.inject.Inject

class ServerRequestHandler(private val appContext: Context, workParams: WorkerParameters) :
    Worker(appContext, workParams) {

    @Inject
    lateinit var requestExecuter: RequestExecuter

    @Inject
    lateinit var requestRepository: RequestRepository

    override fun doWork(): Result {
        DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(appContext))
            .build()
            .inject(this)
        updatePendingRequests()

        Log.d("SERVER_HANDLER", "Successful server handling of requests")

        return Result.success()
    }

    private fun updatePendingRequests() {
        val store = appContext.getSharedPreferences(appContext.getString(R.string.identifiers), Context.MODE_PRIVATE)
        var user = store.getString("public_key_complete", "test")!!
        user = user.replace('+', '-').replace('/', '_')
        val pw = store.getString("password", "testPw")!!
        val pendingRequests = requestRepository.getPendingRequests(user, pw)

        for (request in pendingRequests) {
            val result = requestExecuter.execute(request)
            requestRepository.insertRequestResult(result)
            requestRepository.deletePendingRequest(request)
        }
        requestRepository.sendOutResults()
    }
}