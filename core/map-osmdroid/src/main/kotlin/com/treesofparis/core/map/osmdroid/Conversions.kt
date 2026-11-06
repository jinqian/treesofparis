package com.treesofparis.core.map.osmdroid

import com.treesofparis.domain.model.GeoBounds
import com.treesofparis.domain.model.GeoPoint
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint as OsmGeoPoint

internal fun GeoPoint.toOsm() = OsmGeoPoint(lat, lon)

internal fun IGeoPoint.toDomain() = GeoPoint(lat = latitude, lon = longitude)

internal fun GeoBounds.toBoundingBox() = BoundingBox(north, east, south, west)

internal fun BoundingBox.toGeoBounds() = GeoBounds(south = latSouth, west = lonWest, north = latNorth, east = lonEast)
