package fr.arbresdeparis.core.data.mapper

import fr.arbresdeparis.core.data.db.FavoriteEntity
import fr.arbresdeparis.core.data.db.FavoriteSnapshotEntity
import fr.arbresdeparis.domain.model.Favorite
import fr.arbresdeparis.domain.model.FavoriteSnapshot
import fr.arbresdeparis.domain.model.GeoPoint

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
