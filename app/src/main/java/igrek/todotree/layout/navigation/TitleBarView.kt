package igrek.todotree.layout.navigation

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import igrek.todotree.R
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.appFactory

class TitleBarView(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RelativeLayout(context, attrs, defStyleAttr) {
    private val appCompatActivity: AppCompatActivity by LazyExtractor(appFactory.appCompatActivity)
    private val navigationMenuController: NavigationMenuController by LazyExtractor(appFactory.navigationMenuController)

    private var title: String? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        parseAttrs(attrs)
        initView(context)
    }

    private fun parseAttrs(attrs: AttributeSet?) {
        val attrsArray = appCompatActivity.obtainStyledAttributes(attrs, R.styleable.TitleBarView)

        title = attrsArray.getText(R.styleable.TitleBarView_title)?.toString()

        attrsArray.recycle()
    }

    private fun initView(context: Context?) {
        inflate(context, R.layout.component_titlebar, this)

        if (title != null) {
            val screenTitleTextView: TextView = findViewById(R.id.screenTitleTextView)
            screenTitleTextView.text = title
        }

        val toolbar1 = findViewById<Toolbar>(R.id.toolbar1)
        appCompatActivity.setSupportActionBar(toolbar1)
        val actionBar = appCompatActivity.supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false)
            actionBar.setDisplayShowHomeEnabled(false)
        }

        val navMenuButton = findViewById<ImageButton>(R.id.navMenuButton)
        navMenuButton.setOnClickListener { navigationMenuController.navDrawerShow() }
    }

}
