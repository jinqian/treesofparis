package fr.arbresdeparis.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A row of the pre-packaged `trees.db`. This entity owns the schema: `data/scripts/update_trees.py` builds the
 * asset from Room's exported schema JSON, so change columns here and re-run the script.
 */
@Entity(tableName = "trees", indices = [Index(value = ["lat", "lon"], name = "index_trees_lat_lon")])
data class TreeEntity(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "common_name") val commonName: String,
    val genus: String?,
    val species: String?,
    val cultivar: String?,
    @ColumnInfo(name = "circumference_cm") val circumferenceCm: Int?,
    @ColumnInfo(name = "height_m") val heightM: Int?,
    val stage: String?,
    val remarkable: Boolean,
    val address: String,
    /** Paris: « PARIS 12E ARRDT », a Bois or a département; other sources: the town, as [city]. */
    val arrondissement: String,
    val lat: Double,
    val lon: Double,
    /** The town the tree stands in (« Paris », « Issy-les-Moulineaux », « Pantin »). */
    val city: String = "Paris",
    /** The dataset it comes from: « paris » or « gpso » ([fr.arbresdeparis.domain.model.TreeSource]). */
    val source: String = "paris",
    /** A height range (« 5 à 10 », in metres) when the source has no exact [heightM]. */
    @ColumnInfo(name = "height_class") val heightClass: String? = null,
)
