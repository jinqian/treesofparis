package fr.arbresdeparis.feature.about

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.arbresdeparis.domain.usecase.GetFloweringSources
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
