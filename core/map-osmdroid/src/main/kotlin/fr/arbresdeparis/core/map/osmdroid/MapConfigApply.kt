package fr.arbresdeparis.core.map.osmdroid

import fr.arbresdeparis.core.map.MapConfig
import org.osmdroid.views.MapView

/** Keeps the user inside [MapConfig.bounds] and the zoom range; the world does not repeat. */
internal fun MapConfig.applyTo(mapView: MapView) {
    mapView.setScrollableAreaLimitDouble(bounds.toBoundingBox())
    mapView.minZoomLevel = minZoom
    mapView.maxZoomLevel = maxZoom
    mapView.isHorizontalMapRepetitionEnabled = false
    mapView.isVerticalMapRepetitionEnabled = false
}
