package com.treesofparis.feature.about

import androidx.lifecycle.ViewModel
import com.treesofparis.domain.model.AppInfo
import com.treesofparis.domain.model.DatasetInfo
import com.treesofparis.domain.usecase.GetFloweringSources
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AboutViewModel
    @Inject
    constructor(
        appInfo: AppInfo,
        datasetInfo: DatasetInfo,
        getFloweringSources: GetFloweringSources,
    ) : ViewModel() {
        val uiState =
            AboutUiState(
                versionName = appInfo.versionName,
                extraction = datasetInfo.toExtractionUi(),
                floweringSheetCount = getFloweringSources().size,
            )
    }
