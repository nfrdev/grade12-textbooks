package com.nfrdev.grade12textbooks.util

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

interface Logger { fun d(message: String); fun e(message: String, throwable: Throwable? = null) }

@Singleton
class TimberLogger @Inject constructor() : Logger {
    override fun d(message: String) = Timber.d(message)
    override fun e(message: String, throwable: Throwable?) = Timber.e(throwable, message)
}

interface CrashReporter { fun record(throwable: Throwable) }
@Singleton
class NoOpCrashReporter @Inject constructor() : CrashReporter { override fun record(throwable: Throwable) = Unit }
