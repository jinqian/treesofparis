package fr.arbresdeparis.core.data.mapper

import fr.arbresdeparis.core.data.db.RemarkableTreeEntity
import fr.arbresdeparis.domain.model.RemarkableStory

internal fun RemarkableTreeEntity.toDomain() =
    RemarkableStory(
        treeId = treeId,
        site = site,
        qualification = qualification,
        summary = summary,
        story = story,
        plantingYear = plantingYear,
        label = label,
        councilDecision = councilDecision,
        signUrl = signUrl,
        photoUrl = photoUrl,
        photoCredit = photoCredit,
    )
