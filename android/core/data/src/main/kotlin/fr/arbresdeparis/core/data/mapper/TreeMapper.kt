package fr.arbresdeparis.core.data.mapper

import fr.arbresdeparis.core.data.db.TaxonLinkEntity
import fr.arbresdeparis.core.data.db.TreeEntity
import fr.arbresdeparis.domain.model.GeoPoint
import fr.arbresdeparis.domain.model.Tree
import fr.arbresdeparis.domain.model.TreeSource
import fr.arbresdeparis.domain.model.WikipediaArticle

internal fun TreeEntity.toDomain(wikipedia: WikipediaArticle? = null) =
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
        wikipedia = wikipedia,
        city = city,
        source = TreeSource.fromId(source),
        heightClass = heightClass,
    )

internal fun TaxonLinkEntity.toDomain() = WikipediaArticle(title = wikipediaTitle, isGenusArticle = genusOnly)
