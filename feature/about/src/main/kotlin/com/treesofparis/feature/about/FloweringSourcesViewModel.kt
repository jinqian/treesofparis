package com.treesofparis.feature.about

import androidx.lifecycle.ViewModel
import com.treesofparis.domain.usecase.GetFloweringSources
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.Collator
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class FloweringSourcesViewModel
    @Inject
    constructor(
        getFloweringSources: GetFloweringSources,
    ) : ViewModel() {
        /** By French common name, so « Érable » sorts with the E. */
        val sources: List<FloweringSourceUi> =
            getFloweringSources().map { it.toUi() }.sortedWith(
                compareBy(Collator.getInstance(Locale.FRENCH)) { it.commonName },
            )
    }
