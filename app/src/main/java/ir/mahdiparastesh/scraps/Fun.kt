package ir.mahdiparastesh.scraps

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat

class Fun {
    companion object {
        fun explode(
            c: Context, v: View, alpha: Float = 1f, dur: Long = 522, max: Float = 4f,
            src: Int = R.drawable.circle_ca
        ) {
            val parent: ConstraintLayout?
            try {
                parent = v.parent as ConstraintLayout
            } catch (e: Exception) {
                return
            }

            val ex = View(c)
            val exLP = ConstraintLayout.LayoutParams(0, 0)
            exLP.topToTop = v.id
            exLP.leftToLeft = v.id
            exLP.rightToRight = v.id
            exLP.bottomToBottom = v.id
            ex.background = ContextCompat.getDrawable(c, src)
            ex.translationX = v.translationX
            ex.translationY = v.translationY
            ex.scaleX = v.scaleX
            ex.scaleY = v.scaleY
            ex.alpha = alpha
            parent.addView(ex, parent.indexOfChild(v), exLP)

            val explode = AnimatorSet().setDuration(dur)
            val hide = ObjectAnimator.ofFloat(ex, View.ALPHA, 0f)
            hide.startDelay = explode.duration / 4
            explode.playTogether(
                ObjectAnimator.ofFloat(ex, View.SCALE_X, ex.scaleX * max),
                ObjectAnimator.ofFloat(ex, View.SCALE_Y, ex.scaleY * max),
                hide
            )
            explode.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    parent.removeView(ex)
                }
            })
            explode.start()
        }

        fun Main.dp(px: Int = 0) = (dm.density * px.toFloat()).toInt()

        fun vis(v: View, b: Boolean = true) {
            v.visibility = if (b) View.VISIBLE else View.GONE
        }

        fun toDeg(angle: Float) = angle * (Math.PI / 180)

        fun fromDeg(num: Float) = num / (Math.PI / 180)
    }
}
