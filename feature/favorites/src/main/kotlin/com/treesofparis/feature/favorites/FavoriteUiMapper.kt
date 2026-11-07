package com.treesofparis.feature.favorites

import com.treesofparis.domain.format.formatArea
import com.treesofparis.domain.format.formatPlace
import com.treesofparis.domain.model.Tree

internal fun Tree.toFavoriteUi() =
    FavoriteUiModel(
        id = id,
        title = commonName,
        subtitle = "${formatPlace(address)} · ${formatArea(arrondissement)}",
        remarkable = remarkable,
    )
