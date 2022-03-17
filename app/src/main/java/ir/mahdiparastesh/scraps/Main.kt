package ir.mahdiparastesh.scraps

import android.animation.*
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Process
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import ir.mahdiparastesh.scraps.Fun.Companion.dp
import ir.mahdiparastesh.scraps.Fun.Companion.fromDeg
import ir.mahdiparastesh.scraps.Fun.Companion.toDeg
import ir.mahdiparastesh.scraps.Fun.Companion.vis
import ir.mahdiparastesh.scraps.databinding.MenuBinding
import kotlin.math.*

class Main : ComponentActivity() {
    lateinit var b: MenuBinding
    lateinit var c: Context
    val dm: DisplayMetrics by lazy { resources.displayMetrics }
    private var loaded = false

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    companion object {
        const val INTENSITY_HIGH = 25000f
        const val INTENSITY_MEDIUM = 60000f
        const val INTENSITY_LOW = 200000f
        const val INTENSITY_ONE = -1f
        const val WHICH = INTENSITY_MEDIUM
        const val SPEED_FACTOR = 0.4f // the higher the slower
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = MenuBinding.inflate(layoutInflater)
        setContentView(b.root)
        c = applicationContext

        // Play Button
        b.play.setOnClickListener {
            Fun.explode(c, b.play, 0.15f)
            slide(1)
        }
        if (!loaded) b.play.animate().apply {
            duration = 2000L
            scaleX(1f)
            scaleY(1f)
            rotation(0f)
            setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    loaded = true
                }
            })
            start()
            val delay = duration / 2L
            object : CountDownTimer(delay, delay) {
                override fun onTick(p0: Long) {}
                override fun onFinish() {
                    Fun.explode(c, b.play, 0.3f, 1500, 8f)
                    ObjectAnimator.ofFloat(b.play, View.TRANSLATION_Z, 0f, 30f).apply {
                        duration = 1000L
                        repeatMode = ValueAnimator.REVERSE
                        repeatCount = ValueAnimator.INFINITE
                        start()
                    }
                }
            }.start()
        } else {
            b.play.scaleX = 1f
            b.play.scaleY = 1f
            b.play.rotation = 0f
        }

        // Energy Animations
        val egd = GradientDrawable()
        egd.shape = GradientDrawable.OVAL
        egd.gradientType = GradientDrawable.RADIAL_GRADIENT
        val ca = ContextCompat.getColor(c, R.color.CA)
        val caAlpha1 = Color.argb(92, Color.red(ca), Color.green(ca), Color.blue(ca))
        egd.colors = intArrayOf(ca, ca, caAlpha1, Color.TRANSPARENT)
        egd.gradientRadius = dm.density * 10f
        b.energy1.background = egd
        b.energy2.background = egd
        vis(b.energy1, false)
        vis(b.energy2, false)

        // Bouncers
        val bouncersNum = if (WHICH == INTENSITY_ONE) 1 else
            (dm.widthPixels.toFloat() * dm.heightPixels.toFloat() / WHICH).toInt()
        for (z in 0 until bouncersNum) {
            val bouncer = bouncer(c)
            b.stars.addView(bouncer)
            bounce(bouncer, (0..360).random().toFloat())
        }

        // Second Slide
        b.secondSlide.translationY = dm.heightPixels.toFloat()
        b.secondSlide.setOnClickListener { }
    }

    override fun onSaveInstanceState(state: Bundle) { // With "outPersistentState" doesn't work!
        state.putBoolean("loaded", loaded)
        super.onSaveInstanceState(state)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restoration(savedInstanceState)
    }

    var tapToExit = false
    override fun onBackPressed() {
        if (slide == 1) {
            slide(0); return; }
        if (!tapToExit) {
            Toast.makeText(c, R.string.tapToExit, Toast.LENGTH_LONG).show()
            tapToExit = true
            object : CountDownTimer(3000, 3000) {
                override fun onTick(p0: Long) {}
                override fun onFinish() {
                    tapToExit = false
                }
            }.start(); return
        }
        moveTaskToBack(true)
        Process.killProcess(Process.myPid())
        kotlin.system.exitProcess(0)
    }


    private fun restoration(state: Bundle?) {
        if (state == null) return
        loaded = state.getBoolean("loaded", false)
    }

    private fun bouncer(c: Context) = View(c).apply {
        layoutParams = ConstraintLayout.LayoutParams(dp(15), dp(15)).apply {
            val par = ConstraintLayout.LayoutParams.PARENT_ID
            topToTop = par
            leftToLeft = par
            rightToRight = par
            bottomToBottom = par
        }
        background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            gradientType = GradientDrawable.RADIAL_GRADIENT
            val ca = ContextCompat.getColor(c, R.color.mBouncer)
            val caAlpha1 = Color.rgb(Color.red(ca), Color.green(ca), Color.blue(ca))
            colors = intArrayOf(ca, ca, caAlpha1, Color.TRANSPARENT)
            gradientRadius = layoutParams.width / 2f
        }
        alpha = 0.5f
    }

    fun bounce(v: View, d: Float) { // TODO: SOME CALCULATIONS ARE WRONG
        val halfSW = dm.widthPixels / 2
        val halfSH = dm.heightPixels / 2
        var tX = v.translationX
        var tY = v.translationY
        val adjacent: Float
        val opposite: Float
        val hypotenuse: Float
        val angleC: Float
        val nextDeg: Float
        val trbl = floatArrayOf(
            abs(-halfSH - tY) - (v.layoutParams.height / 2f),
            abs(halfSW - tX) - (v.layoutParams.width / 2f),
            abs(halfSH - tY) - (v.layoutParams.height / 2f),
            abs(-halfSW - tX) - (v.layoutParams.width / 2f)
        )
        val angles = FloatArray(8)
        angles[0] = aSine((sin(toDeg(90f)).toFloat() * trbl[1]) / sep(trbl[1], trbl[0]))
        angles[1] = otherAng(angles[0])
        angles[2] = aSine((sin(toDeg(90f)).toFloat() * trbl[2]) / sep(trbl[2], trbl[1]))
        angles[3] = otherAng(angles[2])
        angles[4] = aSine((sin(toDeg(90f)).toFloat() * trbl[3]) / sep(trbl[3], trbl[2]))
        angles[5] = otherAng(angles[4])
        angles[6] = aSine((sin(toDeg(90f)).toFloat() * trbl[0]) / sep(trbl[0], trbl[3]))
        angles[7] = otherAng(angles[6])

        when {// Summarize at the final moment
            d >= 0f && d < 0f + angles[0] -> {// 1
                adjacent = trbl[0]
                angleC = otherAng(d)
                opposite = (adjacent * sin(toDeg(d)).toFloat()) / sin(toDeg(angleC)).toFloat()
                tX += opposite
                tY -= adjacent
                nextDeg = 90f + angleC
            }
            d >= 0f + angles[0] && d < 90f -> {// 2
                adjacent = trbl[1]
                angleC = otherAng(90f - d)
                opposite = (adjacent * sin(toDeg(90f - d)).toFloat()) / sin(toDeg(angleC)).toFloat()
                tX += adjacent
                tY -= opposite
                nextDeg = 270f + angleC
            }
            d >= 90f && d < 90f + angles[2] -> {// 3
                adjacent = trbl[1]
                angleC = otherAng(d - 90f)
                opposite = (adjacent * sin(toDeg(d - 90f)).toFloat()) / sin(toDeg(angleC)).toFloat()
                tX += adjacent
                tY += opposite
                nextDeg = 180f + angleC
            }
            d >= 90f + angles[2] && d < 180f -> {// 4
                adjacent = trbl[2]
                angleC = otherAng(180f - d)
                opposite =
                    (adjacent * sin(toDeg(180f - d)).toFloat()) / sin(toDeg(angleC)).toFloat()
                tX += opposite
                tY += adjacent
                nextDeg = 90f - angleC
            }
            d >= 180f && d < 180f + angles[4] -> {// 5
                adjacent = trbl[2]
                angleC = otherAng(d - 180f)
                opposite =
                    (adjacent * sin(toDeg(d - 180f)).toFloat()) / sin(toDeg(angleC)).toFloat()
                tX -= opposite
                tY += adjacent
                nextDeg = 270f + angleC
            }
            d >= 180f + angles[4] && d < 270f -> {// 6
                adjacent = trbl[3]
                angleC = otherAng(270f - d)
                opposite =
                    (adjacent * sin(toDeg(270f - d)).toFloat()) / sin(toDeg(angleC)).toFloat()
                tX -= adjacent
                tY += opposite
                nextDeg = 180f - angleC
            }
            d >= 270f && d < 270f + angles[6] -> {// 7
                adjacent = trbl[3]
                angleC = otherAng(d - 270f)
                opposite =
                    (adjacent * sin(toDeg(d - 270f)).toFloat()) / sin(toDeg(angleC)).toFloat()
                tX -= adjacent
                tY -= opposite
                nextDeg = angleC
            }
            d >= 270f + angles[6] && d < 360f -> {// 8
                adjacent = abs(-halfSH - tY) - (v.layoutParams.height / 2f)
                angleC = otherAng(360f - d)
                opposite =
                    (adjacent * sin(toDeg(360f - d)).toFloat()) / sin(toDeg(angleC)).toFloat()
                tX -= opposite
                tY -= adjacent
                nextDeg = 270f - angleC
            }
            else -> {
                (v.parent as ViewGroup).removeView(v); return; }
        }
        hypotenuse = (adjacent * sin(toDeg(90f)).toFloat()) / sin(toDeg(angleC)).toFloat()

        AnimatorSet().apply {
            duration = (hypotenuse * SPEED_FACTOR).toLong()
            interpolator = LinearInterpolator()
            playTogether(
                ObjectAnimator.ofFloat(v, View.TRANSLATION_X, tX),
                ObjectAnimator.ofFloat(v, View.TRANSLATION_Y, tY)
            )
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    bounce(v, nextDeg)
                }
            })
            start()
        }
    }

    private fun sep(b: Float, c: Float) = sqrt(
        (b.toDouble().pow(2.0) + c.toDouble().pow(2.0)) - ((2f * b * c) * cos(toDeg(90f)))
    ).toFloat()

    private fun aSine(x: Float) = fromDeg(asin(x)).toFloat()

    private fun otherAng(a: Float) = 180f - 90f - a


    private var slide = 0
    private var sliding = false
    private fun slide(i: Int) {
        if (slide == i || sliding) return
        sliding = true
        AnimatorSet().apply {
            duration = 650
            playTogether(
                ObjectAnimator.ofFloat(b.firstSlide, View.SCALE_X, if (i == 0) 1f else 0.8f),
                ObjectAnimator.ofFloat(b.firstSlide, View.SCALE_Y, if (i == 0) 1f else 0.8f),
                ObjectAnimator.ofFloat(
                    b.secondSlide, View.TRANSLATION_Y, if (i == 1) 0f else dm.heightPixels.toFloat()
                )
            )
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    if (i == 1) vis(b.secondSlide)
                }

                override fun onAnimationEnd(animation: Animator?) {
                    if (i == 0) vis(b.secondSlide, false)
                    sliding = false
                }
            })
            start()
        }
        slide = i
    }
}
