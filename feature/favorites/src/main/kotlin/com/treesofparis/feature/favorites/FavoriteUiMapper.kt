package com.treesofparis.feature.favorites

import com.treesofparis.domain.format.formatArea
import com.treesofparis.domain.format.formatPlace
import com.treesofparis.domain.model.FavoriteTree

internal fun FavoriteTree.toFavoriteUi(): FavoriteUiModel =
    when (this) {
        is FavoriteTree.Present -> {
            FavoriteUiModel(
                id = id,
                title = tree.commonName,
                subtitle = place(tree.arrondissement, tree.address),
                remarkable = tree.remarkable,
            )
        }

        is FavoriteTree.Gone -> {
            FavoriteUiModel(
                id = id,
                title = snapshot?.commonName,
                subtitle = snapshot?.let { place(it.arrondissement, it.address) },
                remarkable = snapshot?.remarkable ?: false,
                gone = true,
            )
        }
    }

private fun place(
    arrondissement: String,
    address: String,
) = "${formatArea(arrondissement)} · ${formatPlace(address)}"
