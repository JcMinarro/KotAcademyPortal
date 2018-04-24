package org.kotlinacademy.mobile.view.news

import activitystarter.MakeActivityStarter
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.marcinmoskala.kotlinandroidviewbindings.bindToSwipeRefresh
import com.marcinmoskala.kotlinandroidviewbindings.bindToVisibility
import kotlinx.android.synthetic.main.activity_news.*
import org.kotlinacademy.common.recycler.BaseRecyclerViewAdapter
import org.kotlinacademy.data.Article
import org.kotlinacademy.data.Info
import org.kotlinacademy.data.News
import org.kotlinacademy.data.Puzzler
import org.kotlinacademy.mobile.R
import org.kotlinacademy.mobile.view.BaseActivity
import org.kotlinacademy.mobile.view.feedback.FeedbackActivityStarter
import org.kotlinacademy.mobile.view.okSnack
import org.kotlinacademy.presentation.news.NewsPresenter
import org.kotlinacademy.presentation.news.NewsView

@MakeActivityStarter
class NewsActivity : BaseActivity(), NewsView {

    private val presenter by presenter { NewsPresenter(this) }

    override var loading by bindToVisibility(R.id.progressView)
    override var refresh by bindToSwipeRefresh(R.id.swipeRefreshView)

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_news)
        super.onCreate(savedInstanceState)
        swipeRefreshView.setOnRefreshListener { presenter.onRefresh() }
        newsListView.layoutManager = LinearLayoutManager(this)
        fab.setOnClickListener { showGeneralCommentScreen() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == COMMENT_CODE && resultCode == Activity.RESULT_OK -> showThankYouForCommentSnack()
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun showList(news: List<News>) {
        val adapters = news.mapNotNull(::newsToAdapter)
        newsListView.adapter = BaseRecyclerViewAdapter(adapters)
    }

    private fun newsToAdapter(news: News) = when (news) {
        is Article -> ArticleItemAdapter(news, this::showNewsCommentScreen)
        is Info -> InfoItemAdapter(news)
        is Puzzler -> PuzzlerItemAdapter(news)
        else -> null
    }

    private fun showNewsCommentScreen(article: Article) {
        FeedbackActivityStarter.startForResult(this, article.id, COMMENT_CODE)
    }

    private fun showGeneralCommentScreen() {
        FeedbackActivityStarter.startForResult(this, COMMENT_CODE)
    }

    private fun showThankYouForCommentSnack() {
        okSnack(R.string.feedback_response)
    }

    companion object {
        val COMMENT_CODE = 144
    }
}