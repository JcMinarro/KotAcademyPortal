package org.kotlinacademy.backend.repositories.db

import org.jetbrains.squash.connection.Transaction
import org.jetbrains.squash.expressions.eq
import org.jetbrains.squash.query.select
import org.jetbrains.squash.query.where
import org.jetbrains.squash.results.ResultRow
import org.jetbrains.squash.results.get
import org.jetbrains.squash.statements.*
import org.kotlinacademy.backend.repositories.db.Database.makeTransaction
import org.kotlinacademy.data.Article
import org.kotlinacademy.parseDateTime

class ArticlesDatabase : ArticlesDatabaseRepository {

    override suspend fun getArticles(): List<Article> = makeTransaction {
        selectArticles()
                .execute()
                .map(::toArticle)
                .toList()
    }

    override suspend fun getAcceptedArticles(): List<Article> = makeTransaction {
        selectArticles()
                .where { ArticlesTable.accepted.eq(true) }
                .execute()
                .map(::toArticle)
                .toList()
    }

    override suspend fun getArticle(id: Int): Article = makeTransaction {
        selectArticles()
                .where { ArticlesTable.id.eq(id) }
                .execute()
                .map(::toArticle)
                .toList()
                .first()
    }

    private fun selectArticles() = ArticlesTable.select(ArticlesTable.id, ArticlesTable.title, ArticlesTable.subtitle, ArticlesTable.imageUrl, ArticlesTable.url, ArticlesTable.occurrence)

    private fun toArticle(it: ResultRow) = Article(
            id = it[ArticlesTable.id],
            title = it[ArticlesTable.title],
            subtitle = it[ArticlesTable.subtitle],
            imageUrl = it[ArticlesTable.imageUrl],
            url = it[ArticlesTable.url],
            occurrence = it[ArticlesTable.occurrence].parseDateTime()
    )

    override suspend fun addArticle(article: Article, isAccepted: Boolean) = makeTransaction {
        insertInto(ArticlesTable).values {
            it[title] = article.title
            it[subtitle] = article.subtitle
            it[imageUrl] = article.imageUrl
            it[url] = article.url
            it[occurrence] = article.occurrence.toDateFormatString()
            it[accepted] = isAccepted
        }.execute()
    }

    override suspend fun deleteArticle(newsId: Int) = makeTransaction {
        deleteFrom(ArticlesTable).where(ArticlesTable.id.eq(newsId)).execute()
    }

    override suspend fun updateArticle(id: Int, article: Article, isAccepted: Boolean?) = makeTransaction {
        require(countArticlesWithId(id) == 1) { "Should be single article with id $id" }
        update(ArticlesTable)
                .where { ArticlesTable.id eq id }
                .set {
                    it[title] = article.title
                    it[subtitle] = article.subtitle
                    it[imageUrl] = article.imageUrl
                    it[url] = article.url
                    it[occurrence] = article.occurrence.toDateFormatString()
                    if (isAccepted != null) it[accepted] = isAccepted
                }.execute()
    }

    private fun Transaction.countArticlesWithId(id: Int) = ArticlesTable.select(ArticlesTable.id)
            .where { ArticlesTable.id.eq(id) }
            .execute()
            .count()
}