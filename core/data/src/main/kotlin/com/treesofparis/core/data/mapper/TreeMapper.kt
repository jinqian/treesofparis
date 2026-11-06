package com.treesofparis.core.data.mapper

import com.treesofparis.core.data.db.TreeEntity
import com.treesofparis.domain.model.GeoPoint
import com.treesofparis.domain.model.Tree

internal fun TreeEntity.toDomain() =
    Tree(
        id = id,
        commonName = commonName,
        genus = genus,
        species = species,
        cultivar = cultivar,
        circumferenceCm = circumferenceCm,
        heightM = heightM,
        stage = stage,
        remarkable = remarkable,
        address = address,
        arrondissement = arrondissement,
        location = GeoPoint(lat = lat, lon = lon),
    )
