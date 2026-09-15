package com.geofencenotifier.workers
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.util.Log

class OutboxRetryWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        Log.i("OutboxRetryWorker", "Retrying failed jobs")
        return Result.success()
    }
}