package com.treesofparis.feature.about

import androidx.lifecycle.ViewModel
import com.treesofparis.domain.model.AppInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AboutViewModel
    @Inject
    constructor(
        appInfo: AppInfo,
    ) : ViewModel() {
        val uiState = AboutUiState(versionName = appInfo.versionName)
    }
