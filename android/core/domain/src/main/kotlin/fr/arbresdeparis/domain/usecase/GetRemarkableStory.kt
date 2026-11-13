package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.model.RemarkableStory
import fr.arbresdeparis.domain.repository.RemarkableStoryRepository
import javax.inject.Inject

class GetRemarkableStory
    @Inject
    constructor(
        private val stories: RemarkableStoryRepository,
    ) {
        suspend operator fun invoke(treeId: Long): RemarkableStory? = stories.getStory(treeId)
    }
