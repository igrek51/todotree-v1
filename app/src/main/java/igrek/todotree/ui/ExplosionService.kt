package igrek.todotree.ui

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.appFactory
import tyrantgit.explosionfield.ExplosionField
import tyrantgit.explosionfield.Utils
import java.util.Arrays
import java.util.Random


class ExplosionService {
    private val activity: Activity by LazyExtractor(appFactory.activity)

    private var mExplosionField: ExplosionField? = null
    private val mExpandInset = IntArray(2)
    private val yOffset = Utils.dp2Px(30)

    fun init() {
        mExplosionField = ExplosionField.attach2Window(activity)
        Arrays.fill(mExpandInset, Utils.dp2Px(160))
    }

    fun explode(coordinates: SizeAndPosition) {
        val r = Rect(
            coordinates.x,
            coordinates.y + yOffset,
            coordinates.x + coordinates.w,
            coordinates.y + coordinates.h + yOffset,
        )
        r.inset(-this.mExpandInset[0], -this.mExpandInset[1])

        val startDelay = 10L
        val duration = 1024L

        mExplosionField?.explode(
            createRandomBitmap(coordinates),
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

    private fun createRandomBitmap(r: SizeAndPosition): Bitmap {
        val bitmap = Bitmap.createBitmap(r.w, r.h, Bitmap.Config.ARGB_8888)
        val alphaChannel = 255 shl 24
        val pixels = IntArray(r.w * r.h) {
            alphaChannel or Random().nextInt(16777216)
        }
        bitmap.setPixels(pixels, 0, r.w, 0, 0, r.w, r.h)
        return bitmap
    }

}

data class SizeAndPosition(
    val x: Int,
    val y: Int,
    val w: Int,
    val h: Int,
)