package com.nfrdev.grade12textbooks.data.download

sealed interface DownloadState {
    data object Idle : DownloadState
    data object Queued : DownloadState
    data class InProgress(val progress: Float?) : DownloadState
    data object Verifying : DownloadState
    data object Completed : DownloadState
    data class Failed(val reason: String) : DownloadState
    data object Paused : DownloadState
}
