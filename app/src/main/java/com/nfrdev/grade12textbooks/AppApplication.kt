package com.nfrdev.grade12textbooks

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.nfrdev.grade12textbooks.data.repository.UpdateChecker

@HiltAndroidApp
class AppApplication : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var updateChecker: UpdateChecker
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()
    override fun onCreate() {
        super.onCreate()
        WorkManager.initialize(this, workManagerConfiguration)
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        CoroutineScope(Dispatchers.IO).launch { updateChecker.check() }
    }
}
