package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.model.RemarkableStory
import fr.arbresdeparis.domain.repository.RemarkableStoryRepository
import javax.inject.Inject

/** Every remarkable tree's story, by tree id. */
class GetRemarkableStories
    @Inject
    constructor(
        private val stories: RemarkableStoryRepository,
    ) {
        suspend operator fun invoke(): Map<Long, RemarkableStory> = stories.getStories().associateBy { it.treeId }
    }
