package fr.arbresdeparis.core.map.osmdroid

import fr.arbresdeparis.core.map.CameraRequest

/** Remembers which [CameraRequest] ids were applied, so each programmatic move happens exactly once. */
internal class CameraRequestTracker {
    private var lastAppliedId: Long? = null

    fun shouldApply(request: CameraRequest?): Boolean {
        if (request == null || request.id == lastAppliedId) return false
        lastAppliedId = request.id
        return true
    }
}
