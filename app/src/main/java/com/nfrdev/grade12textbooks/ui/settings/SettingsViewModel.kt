package com.nfrdev.grade12textbooks.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfrdev.grade12textbooks.data.repository.UpdateChecker
import com.nfrdev.grade12textbooks.data.repository.UpdateInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(private val checker: UpdateChecker) : ViewModel() {
    private val _update = MutableStateFlow<UpdateInfo?>(null)
    val update: StateFlow<UpdateInfo?> = _update
    fun checkForUpdates() { viewModelScope.launch { _update.value = checker.check() } }
}
