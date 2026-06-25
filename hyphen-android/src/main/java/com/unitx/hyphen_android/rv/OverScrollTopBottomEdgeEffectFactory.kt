package com.unitx.hyphen_android.rv

import android.graphics.Canvas
import android.widget.EdgeEffect
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView


class OverScrollTopBottomEdgeEffectFactory : RecyclerView.EdgeEffectFactory() {

    // Define constants for the desired overscroll magnitude.
    // Adjust these values to control how much the view translates.
    // A larger multiplier makes the overscroll more pronounced.
    private val OVERSCROLL_MULTIPLIER = 0.5f // Adjust this value (e.g., 0.5f to 2.0f)
    private val MAX_OVERSCROLL_TRANSLATION_PX = 1000f // Cap the maximum translation to prevent extreme values

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
                val sign = if (direction == DIRECTION_BOTTOM) -1 else 1 // -1 for bottom, 1 for top

                // Calculate the raw overscroll translation.
                // We multiply deltaDistance by a large number to get the desired "5x to 10k" effect.
                // The actual value will depend on the scale of your UI.
                var overscrollTranslation = deltaDistance * OVERSCROLL_MULTIPLIER * recyclerView.height // Use RecyclerView height for a more relative scale

                // Apply the sign to the translation.
                overscrollTranslation *= sign

                // Add the new translation to the current translationY of the RecyclerView.
                // We're essentially accumulating the overscroll.
                var newTranslationY = recyclerView.translationY + overscrollTranslation

                // Cap the translation to prevent it from going too far.
                // This ensures the effect remains controlled and doesn't scroll "10k pixels" literally unless you want it to.
                newTranslationY = newTranslationY.coerceIn(-MAX_OVERSCROLL_TRANSLATION_PX, MAX_OVERSCROLL_TRANSLATION_PX)

                recyclerView.translationY = newTranslationY

                // Invalidate to trigger redraw
                recyclerView.invalidate()
            }

            override fun onRelease() {
                super.onRelease()
                // When released, animate back to original position (translationY = 0f)
                if (recyclerView.translationY != 0f) {
                    springAnimation?.cancel() // Cancel any ongoing animation

                    springAnimation = SpringAnimation(recyclerView, SpringAnimation.TRANSLATION_Y).apply {
                        spring = SpringForce().apply {
                            finalPosition = 0f // Animate back to the original position
                            // Adjust dampingRatio and stiffness for desired bounce behavior
                            dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY // More bouncy
                            stiffness = SpringForce.STIFFNESS_LOW // Softer feel
                        }
                        start()
                    }
                }
            }

            override fun onAbsorb(velocity: Int) {
                super.onAbsorb(velocity)
                // This is called when a fling gesture goes past the edge.
                // We want to animate the bounce back based on the velocity of the fling.

                val sign = if (direction == DIRECTION_BOTTOM) -1 else 1 // -1 for bottom, 1 for top

                // Adjust the starting velocity for the bounce back.
                // A higher multiplier here means a stronger bounce.
                val translationVelocity = sign * velocity * 0.5f // Adjust this multiplier

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

            // Optional: You might want to override isFinished() if you have custom ending conditions
            // but for simple translation, the default behavior is usually fine.
            override fun isFinished(): Boolean {
                return springAnimation == null || springAnimation?.isRunning == false
            }
        }
    }
}