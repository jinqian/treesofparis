package com.treesofparis.core.data.mapper

import com.treesofparis.core.data.db.FavoriteEntity
import com.treesofparis.core.data.db.FavoriteSnapshotEntity
import com.treesofparis.domain.model.Favorite
import com.treesofparis.domain.model.FavoriteSnapshot
import com.treesofparis.domain.model.GeoPoint

internal fun FavoriteEntity.toDomain() = Favorite(treeId = treeId, snapshot = snapshot?.toDomain())

internal fun FavoriteSnapshotEntity.toDomain() =
    FavoriteSnapshot(
        commonName = commonName,
        address = address,
        arrondissement = arrondissement,
        remarkable = remarkable,
        location = GeoPoint(lat, lon),
    )

internal fun FavoriteSnapshot.toEntity() =
    FavoriteSnapshotEntity(
        commonName = commonName,
        address = address,
        arrondissement = arrondissement,
        remarkable = remarkable,
        lat = location.lat,
        lon = location.lon,
    )
