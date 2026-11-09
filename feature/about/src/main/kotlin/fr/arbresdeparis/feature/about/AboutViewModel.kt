package fr.arbresdeparis.feature.about

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.arbresdeparis.domain.model.AppInfo
import fr.arbresdeparis.domain.model.DatasetInfo
import fr.arbresdeparis.domain.usecase.GetFloweringSources
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
