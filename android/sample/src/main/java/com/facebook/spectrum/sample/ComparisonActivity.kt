// Copyright (c) Facebook, Inc. and its affiliates.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.spectrum.sample

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnNextLayout
import com.facebook.spectrum.sample.databinding.ComparisonActivityBinding
import com.facebook.spectrum.sample.model.ComparisonViewModel
import kotlin.math.min

class ComparisonActivity : AppCompatActivity() {
  private lateinit var binding: ComparisonActivityBinding
  private val mDisplayMetrics = DisplayMetrics()

  private var mScaleFactor = 1.0f
  private var mScaleGestureDetector: ScaleGestureDetector? = null
  private var mGestureDetector: GestureDetector? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ComparisonActivityBinding.inflate(layoutInflater)
    setContentView(binding.root)
    supportActionBar?.hide()

    mScaleGestureDetector = ScaleGestureDetector(this, ScaleListener())
    mGestureDetector = GestureDetector(this, GestureListener())

    binding.newImage.doOnNextLayout {
      val parent = binding.newImage.parent as View
      mDisplayMetrics.widthPixels = parent.width
      mDisplayMetrics.heightPixels = parent.height
    }

    mDisplayMetrics.widthPixels = (binding.newImage.parent as View).width
    mDisplayMetrics.heightPixels = (binding.newImage.parent as View).height

    val model = ComparisonViewModel.INSTANCE
    binding.newImage.setImageBitmap(model.outputBitmap)
    binding.oldImage.setImageBitmap(model.inputBitmap)

    binding.comparisonBar.setOnSeekBarChangeListener(
        object : SeekBar.OnSeekBarChangeListener {
          override fun onProgressChanged(seekBar: SeekBar, progressValue: Int, fromUser: Boolean) {
            binding.newImage.alpha = progressValue.toFloat() / 100f
          }

          override fun onStartTrackingTouch(seekBar: SeekBar) {}

          override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
  }

  override fun onTouchEvent(ev: MotionEvent): Boolean {
    mScaleGestureDetector?.onTouchEvent(ev)
    mGestureDetector?.onTouchEvent(ev)
    return true
  }

  private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
    override fun onScale(detector: ScaleGestureDetector): Boolean {
      mScaleFactor = (mScaleFactor * detector.scaleFactor).coerceIn(1f, 10f)

      binding.newImage.scaleX = mScaleFactor
      binding.newImage.scaleY = mScaleFactor
      binding.oldImage.scaleX = mScaleFactor
      binding.oldImage.scaleY = mScaleFactor

      setImageTranslation(0f, 0f)
      return true
    }
  }

  private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
      setImageTranslation(distanceX, distanceY)
      return true
    }
  }

  private fun setImageTranslation(distanceX: Float, distanceY: Float) {
    val scaledImageWidth = binding.newImage.width * mScaleFactor
    val scaledImageHeight = binding.newImage.height * mScaleFactor
    val limitWidth = mDisplayMetrics.widthPixels.toFloat()
    val limitHeight = mDisplayMetrics.heightPixels.toFloat()
    var newX = binding.newImage.translationX - distanceX
    var newY = binding.newImage.translationY - distanceY

    val leftEdge = newX - (scaledImageWidth - limitWidth) / 2
    val topEdge = newY - (scaledImageHeight - limitHeight) / 2

    newX +=
        getOffset(
            leftEdge, leftEdge + scaledImageWidth, 0f, limitWidth, leftEdge + scaledImageWidth / 2)
    newY +=
        getOffset(
            topEdge, topEdge + scaledImageHeight, 0f, limitHeight, topEdge + scaledImageHeight / 2)

    binding.newImage.translationX = newX
    binding.newImage.translationY = newY
    binding.oldImage.translationX = newX
    binding.oldImage.translationY = newY
  }

  private fun mean(a: Float, b: Float) = (a + b) / 2

  private fun getOffset(
      imageStart: Float,
      imageEnd: Float,
      limitStart: Float,
      limitEnd: Float,
      limitCenter: Float
  ): Float {
    val imageWidth = imageEnd - imageStart
    val limitWidth = limitEnd - limitStart
    val limitInnerWidth = min(limitCenter - limitStart, limitEnd - limitCenter) * 2
    // center if smaller than limitInnerWidth
    if (imageWidth < limitInnerWidth) {
      return limitCenter - mean(imageEnd, imageStart)
    }
    // to the edge if in between and limitCenter is not (limitLeft + limitRight) / 2
    if (imageWidth < limitWidth) {
      return when {
        limitCenter < mean(limitStart, limitEnd) -> limitStart - imageStart
        else -> limitEnd - imageEnd
      }
    }
    // to the edge if larger than limitWidth and empty space visible
    return when {
      imageStart > limitStart -> limitStart - imageStart
      imageEnd < limitEnd -> limitEnd - imageEnd
      else -> 0f
    }
  }
}
