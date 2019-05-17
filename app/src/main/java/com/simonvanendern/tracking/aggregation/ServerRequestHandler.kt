package com.simonvanendern.tracking.aggregation

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simonvanendern.tracking.repository.RequestRepository
import javax.inject.Inject

class ServerRequestHandler(appContext: Context, workParams: WorkerParameters) :
    Worker(appContext, workParams) {

    @Inject
    lateinit var requestExecuter: RequestExecuter

    @Inject
    lateinit var requestRepository: RequestRepository

    private val pendingRequests = requestRepository.getPendingRequests("test")

    override fun doWork(): Result {
        updatePendingRequests()

        Log.d("SERVER_HANDLER", "Successful server handling")

        return Result.success()
    }

    private fun updatePendingRequests() {
        for (request in pendingRequests) {
            val result = requestExecuter.execute(request)
            requestRepository.insertRequestResult(result)
            requestRepository.deletePendingRequest(result)
        }
        requestRepository.sendOutResults()
//        for (res in requestRepository.getRequestResults()) {
//
//        }
    }
}