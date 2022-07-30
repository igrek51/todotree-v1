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
        Arrays.fill(mExpandInset, Utils.dp2Px(160))
    }

    fun explode(view: View?) {
        if (view == null)
            return

        val r = Rect()
        view.getGlobalVisibleRect(r)
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        r.inset(-this.mExpandInset[0], -this.mExpandInset[1])

        val startDelay = 10L
        val duration = 1024L

        mExplosionField?.explode(
            createRandomBitmap(view),
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

    private fun createRandomBitmap(v: View): Bitmap {
        val bitmap = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        val alphaChannel = 255 shl 24
        val pixels = IntArray(v.width * v.height) {
            alphaChannel or Random().nextInt(16777216)
        }
        bitmap.setPixels(pixels, 0, v.width, 0, 0, v.width, v.height)
        return bitmap
    }

}