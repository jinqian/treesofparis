package fr.arbresdeparis.core.data.repository

import fr.arbresdeparis.core.data.db.RemarkableStoryDao
import fr.arbresdeparis.core.data.mapper.toDomain
import fr.arbresdeparis.domain.model.RemarkableStory
import fr.arbresdeparis.domain.repository.RemarkableStoryRepository
import javax.inject.Inject

internal class RemarkableStoryRepositoryImpl
    @Inject
    constructor(
        private val dao: RemarkableStoryDao,
    ) : RemarkableStoryRepository {
        override suspend fun getStory(treeId: Long): RemarkableStory? = dao.byTreeId(treeId)?.toDomain()

        override suspend fun getStories(): List<RemarkableStory> = dao.all().map { it.toDomain() }
    }
