package fr.arbresdeparis.domain.repository

import fr.arbresdeparis.domain.model.RemarkableStory

/**
 * The stories of the remarkable trees of Paris. The other trees, and the remarkable trees of the other towns, have
 * none.
 */
interface RemarkableStoryRepository {
    suspend fun getStory(treeId: Long): RemarkableStory?

    /** Every story, in no particular order. */
    suspend fun getStories(): List<RemarkableStory>
}
