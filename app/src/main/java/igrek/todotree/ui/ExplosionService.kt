package igrek.todotree.ui

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import tyrantgit.explosionfield.ExplosionField
import tyrantgit.explosionfield.Utils
import java.util.*

class ExplosionService(val activity: Activity) {

    private var mExplosionField: ExplosionField? = null
    private val mExpandInset = IntArray(2)

    fun init() {
        mExplosionField = ExplosionField.attach2Window(activity)
        Arrays.fill(mExpandInset, Utils.dp2Px(32))
    }

    fun explode(view: View) {
        val r = Rect()
        view.getGlobalVisibleRect(r)
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        r.offset(-location[0], -location[1])
        r.inset(-this.mExpandInset[0], -this.mExpandInset[1])

        r.offset(0, 300)
        //val bounds = Rect(view.left, view.top, view.left + view.width, view.top + view.height)

        val startDelay = 100L
        val duration = 1024L

        mExplosionField?.explode(
            getBitmapFromView(view),
            r,
            startDelay,
            duration,
        )
    }

    private fun getBitmapFromView(v: View): Bitmap {
        val bitmap = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        v.draw(canvas)
        return bitmap
    }

}