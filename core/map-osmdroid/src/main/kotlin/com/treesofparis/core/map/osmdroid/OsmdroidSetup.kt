package com.treesofparis.core.map.osmdroid

import android.content.Context
import org.osmdroid.config.Configuration
import java.io.File

/**
 * Global osmdroid configuration. The user agent identifies the app to the OSM tile servers (required by the OSM
 * tile usage policy) and the tile cache lives in the app cache dir, so no storage permission is needed.
 */
internal fun configureOsmdroid(context: Context) {
    val config = Configuration.getInstance()
    config.userAgentValue = context.packageName
    val base = File(context.cacheDir, "osmdroid")
    config.osmdroidBasePath = base
    config.osmdroidTileCache = File(base, "tiles")
}
