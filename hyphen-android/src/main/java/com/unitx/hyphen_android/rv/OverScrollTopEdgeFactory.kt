package com.unitx.hyphen_android.rv

import android.graphics.Canvas
import android.widget.EdgeEffect
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView

class OverScrollTopEdgeFactory : RecyclerView.EdgeEffectFactory() {

    // --- Constants for Top Overscroll (scrolling down from top) ---
    private val topOverScrollMultiplier = 0.5f // Adjust for desired "5x to 10k" effect magnitude
    private val maxTopOverScrollTranslationPx = 1000f // Cap the maximum downward translation

    // --- Constants for Bottom Overscroll (scrolling up from bottom) ---
    private val bottomOverScrollMultiplier = 0.1f // Smaller multiplier for subtle bounce
    private val maxBottomOverScrollTranslationPx = 150f // Cap the maximum upward translation (e.g., 150px)


    override fun createEdgeEffect(recyclerView: RecyclerView, direction: Int): EdgeEffect {
        return object : EdgeEffect(recyclerView.context) {

            private var springAnimation: SpringAnimation? = null

            override fun onPull(deltaDistance: Float) {
                super.onPull(deltaDistance)
                handlePull(recyclerView, direction, deltaDistance)
            }

            override fun onPull(deltaDistance: Float, displacement: Float) {
                super.onPull(deltaDistance, displacement)
                handlePull(recyclerView, direction, deltaDistance)
            }

            private fun handlePull(recyclerView: RecyclerView, direction: Int, deltaDistance: Float) {
                val overscrollTranslation: Float
                var newTranslationY: Float

                when (direction) {
                    DIRECTION_TOP -> {
                        // Calculate overscroll for top (pulling down)
                        overscrollTranslation = deltaDistance * topOverScrollMultiplier * recyclerView.height
                        newTranslationY = recyclerView.translationY + overscrollTranslation
                        // Clamp translation: should be positive or zero, up to MAX_TOP_OVERSCROLL_TRANSLATION_PX
                        newTranslationY = newTranslationY.coerceAtMost(maxTopOverScrollTranslationPx).coerceAtLeast(0f)
                    }
                    DIRECTION_BOTTOM -> {
                        // Calculate overscroll for bottom (pulling up)
                        // Note: deltaDistance is always positive, so we apply a negative sign for upward movement
                        overscrollTranslation = -deltaDistance * bottomOverScrollMultiplier * recyclerView.height
                        newTranslationY = recyclerView.translationY + overscrollTranslation

                        // Clamp translation: should be negative or zero, down to -MAX_BOTTOM_OVERSCROLL_TRANSLATION_PX
                        newTranslationY = newTranslationY.coerceAtLeast(-maxBottomOverScrollTranslationPx).coerceAtMost(0f)
                    }
                    else -> return // Do nothing for other directions (shouldn't happen with RecyclerView)
                }

                recyclerView.translationY = newTranslationY
                recyclerView.invalidate() // Invalidate to trigger redraw
            }

            override fun onRelease() {
                super.onRelease()
                // When released, animate back to original position (translationY = 0f)
                if (recyclerView.translationY != 0f) {
                    springAnimation?.cancel() // Cancel any ongoing animation

                    springAnimation = SpringAnimation(recyclerView, SpringAnimation.TRANSLATION_Y).apply {
                        spring = SpringForce().apply {
                            finalPosition = 0f // Animate back to the original position
                            dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY // Adjust for desired bounce
                            stiffness = SpringForce.STIFFNESS_LOW // Softer feel
                        }
                        start()
                    }
                }
            }

            override fun onAbsorb(velocity: Int) {
                super.onAbsorb(velocity)

                val translationVelocity: Float = when (direction) {
                    DIRECTION_TOP -> {
                        // Fling from top (downward)
                        velocity * 0.5f // Positive velocity for downward fling
                    }

                    DIRECTION_BOTTOM -> {
                        // Fling from bottom (upward)
                        -velocity * 0.2f // Negative velocity for upward fling, smaller multiplier
                    }

                    else -> return
                }

                springAnimation?.cancel()

                springAnimation = SpringAnimation(recyclerView, SpringAnimation.TRANSLATION_Y).apply {
                    spring = SpringForce().apply {
                        finalPosition = 0f
                        dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                        stiffness = SpringForce.STIFFNESS_LOW
                    }
                    setStartVelocity(translationVelocity) // Apply the fling velocity to the animation
                    start()
                }
            }

            override fun draw(canvas: Canvas?): Boolean {
                // Return false if you don't want to draw the default edge effect glow/stretch.
                // We are handling the visual effect through translationY.
                return false
            }

            override fun isFinished(): Boolean {
                return springAnimation == null || springAnimation?.isRunning == false
            }
        }
    }
}