package fr.arbresdeparis.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The story of a remarkable tree of Paris, from « Les arbres remarquables » (Paris Data, ODbL), built by
 * `data/scripts/remarkable.py`. [treeId] is the tree's id in `trees`. [qualification] says why it is remarkable
 * (« Paysager », « Botanique », « Symbolique », « Historique »); [plantingYear] is null when unknown. [signUrl] and
 * [photoUrl] point to the City's server.
 */
@Entity(tableName = "remarkable_trees")
data class RemarkableTreeEntity(
    @PrimaryKey @ColumnInfo(name = "tree_id") val treeId: Long,
    val site: String?,
    val qualification: String?,
    val summary: String?,
    val story: String?,
    @ColumnInfo(name = "planting_year") val plantingYear: Int?,
    val label: String?,
    @ColumnInfo(name = "council_decision") val councilDecision: String?,
    @ColumnInfo(name = "sign_url") val signUrl: String?,
    @ColumnInfo(name = "photo_url") val photoUrl: String?,
    @ColumnInfo(name = "photo_credit") val photoCredit: String?,
)
