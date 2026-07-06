package com.foodmaster.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.foodmaster.app.FoodMasterApplication
import java.util.concurrent.TimeUnit

/**
 * Daily check that notifies about products expiring within 3 days. Pulls the
 * repository from the [FoodMasterApplication] container (manual DI).
 */
class ExpiryCheckWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? FoodMasterApplication ?: return Result.success()
        val soon = app.container.inventoryRepository.itemsExpiringWithin(days = 3)
        ExpiryNotifier.notifyExpiring(applicationContext, soon)
        return Result.success()
    }

    companion object {
        private const val UNIQUE_NAME = "expiry-check"

        /** Enqueue the daily periodic check (keeps any already-scheduled work). */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(1, TimeUnit.DAYS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
