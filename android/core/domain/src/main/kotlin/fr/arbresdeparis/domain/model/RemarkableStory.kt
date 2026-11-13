package fr.arbresdeparis.domain.model

/**
 * What the City says about a remarkable tree of Paris (« Les arbres remarquables », Paris Data): where it stands
 * ([site]), why it is remarkable ([qualification]: « Paysager », « Botanique », « Symbolique », « Historique »), a
 * one-sentence [summary], its [story], its planting year when known, its national label and council decision when it
 * has one, and the City's sign (PDF) and photo, with the photographer's credit.
 */
data class RemarkableStory(
    val treeId: Long,
    val site: String?,
    val qualification: String?,
    val summary: String?,
    val story: String?,
    val plantingYear: Int?,
    val label: String?,
    val councilDecision: String?,
    val signUrl: String?,
    val photoUrl: String?,
    val photoCredit: String?,
)
