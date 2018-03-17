package org.kotlinacademy.backend.usecases

import org.kotlinacademy.backend.logInfo
import org.kotlinacademy.backend.repositories.db.ArticlesDatabaseRepository
import org.kotlinacademy.backend.repositories.network.MediumRepository

object MediumUseCase {

    suspend fun sync(mediumRepository: MediumRepository, articlesDatabaseRepository: ArticlesDatabaseRepository) {
        val news = mediumRepository.getNews()
        if (news == null || news.isEmpty()) {
            logInfo("Medium did not succeed when processing request")
            return
        }

        val prevNewsTitles = articlesDatabaseRepository.getArticles().map { it.title }
        news.filter { it.title !in prevNewsTitles }
                .forEach { article -> articlesDatabaseRepository.addArticle(article, true) }
    }
}