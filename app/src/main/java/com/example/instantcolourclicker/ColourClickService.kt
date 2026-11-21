package com.example.instantcolourclicker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.ScreenshotResult
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import java.util.concurrent.Executors
import kotlin.math.max

class ColourClickService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()
    private var busy = false
    private val loop = object : Runnable {
        override fun run() {
            val enabled = getSharedPreferences("clicker", MODE_PRIVATE).getBoolean("enabled", false)
            if (enabled && !busy) captureAndTap()
            handler.postDelayed(this, 100L)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        handler.post(loop)
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    private fun captureAndTap() {
        busy = true
        try {
            takeScreenshot(Display.DEFAULT_DISPLAY, executor, object : TakeScreenshotCallback {
                override fun onSuccess(result: ScreenshotResult) {
                    try {
                        val hw = result.hardwareBuffer
                        val bitmap = Bitmap.wrapHardwareBuffer(hw, result.colorSpace)
                        hw.close()
                        if (bitmap != null) {
                            val point = findLargestBrightRegion(bitmap)
                            bitmap.recycle()
                            if (point != null) tap(point.first.toFloat(), point.second.toFloat())
                        }
                    } catch (_: Exception) {
                    } finally {
                        busy = false
                    }
                }
                override fun onFailure(errorCode: Int) { busy = false }
            })
        } catch (_: Exception) { busy = false }
    }

    // Detect non-black coloured pixels, group adjacent pixels, and tap
    // the centre of the largest connected coloured region.
    private fun findLargestBrightRegion(bitmap: Bitmap): Pair<Int, Int>? {
        val step = 5
        val cols = (bitmap.width + step - 1) / step
        val rows = (bitmap.height + step - 1) / step
        val mask = BooleanArray(cols * rows)

        for (gy in 0 until rows) {
            for (gx in 0 until cols) {
                val c = bitmap.getPixel(
                    (gx * step).coerceAtMost(bitmap.width - 1),
                    (gy * step).coerceAtMost(bitmap.height - 1)
                )
                val r = Color.red(c)
                val g = Color.green(c)
                val b = Color.blue(c)
                // Ignore black/dark background; accept bright pixels of any hue.
                mask[gy * cols + gx] = max(r, max(g, b)) > 75 && (r + g + b) > 190
            }
        }

        val visited = BooleanArray(mask.size)
        val queue = java.util.ArrayDeque<Int>()
        var bestMinX = 0; var bestMinY = 0
        var bestMaxX = -1; var bestMaxY = -1
        var bestSize = 0

        for (i in mask.indices) {
            if (!mask[i] || visited[i]) continue
            var minX = cols; var minY = rows; var maxX = 0; var maxY = 0
            var size = 0
            visited[i] = true
            queue.add(i)
            while (queue.isNotEmpty()) {
                val cur = queue.removeFirst()
                val x = cur % cols
                val y = cur / cols
                size++
                minX = minOf(minX, x); minY = minOf(minY, y)
                maxX = maxOf(maxX, x); maxY = maxOf(maxY, y)
                val neighbours = intArrayOf(
                    if (x > 0) cur - 1 else -1,
                    if (x + 1 < cols) cur + 1 else -1,
                    if (y > 0) cur - cols else -1,
                    if (y + 1 < rows) cur + cols else -1
                )
                for (n in neighbours) {
                    if (n >= 0 && mask[n] && !visited[n]) {
                        visited[n] = true
                        queue.add(n)
                    }
                }
            }
            if (size > bestSize) {
                bestSize = size
                bestMinX = minX; bestMinY = minY
                bestMaxX = maxX; bestMaxY = maxY
            }
        }

        if (bestSize < 12 || bestMaxX < bestMinX || bestMaxY < bestMinY) return null
        val centerX = (((bestMinX + bestMaxX) / 2) * step + step / 2)
            .coerceIn(0, bitmap.width - 1)
        val centerY = (((bestMinY + bestMaxY) / 2) * step + step / 2)
            .coerceIn(0, bitmap.height - 1)
        return Pair(centerX, centerY)
    }

    private fun tap(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 1))
            .build()
        dispatchGesture(gesture, null, null)
    }

    override fun onDestroy() {
        handler.removeCallbacks(loop)
        executor.shutdown()
        super.onDestroy()
    }
}
