package com.example.clockwidgetstudio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ClockRenderer {
    const val PREFS = "clock_widget_prefs"
    const val KEY_MODEL = "model"
    const val KEY_BG = "background"

    fun render(context: Context): Bitmap {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val model = prefs.getInt(KEY_MODEL, 0)
        val bg = prefs.getInt(KEY_BG, 1)
        val bitmap = Bitmap.createBitmap(1000, 420, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val background = when (bg) {
            0 -> Color.TRANSPARENT
            1 -> Color.rgb(49, 49, 96)
            2 -> Color.rgb(8, 8, 10)
            else -> Color.rgb(245, 245, 248)
        }
        canvas.drawColor(background)

        val light = if (bg == 3) Color.rgb(70, 68, 105) else Color.rgb(225, 222, 255)
        val accent = if (bg == 3) Color.rgb(75, 72, 110) else Color.rgb(173, 166, 235)

        val date = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            .format(Date()).uppercase(Locale.getDefault())
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = light
            textSize = 38f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
        canvas.drawText(date, 500f, 62f, datePaint)

        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent
            style = Paint.Style.STROKE
            strokeWidth = 7f
        }
        canvas.drawRoundRect(RectF(45f, 95f, 955f, 385f), 42f, 42f, framePaint)

        val time = SimpleDateFormat("HHmm", Locale.getDefault()).format(Date())
        if (model == 0) drawOutlineSolid(canvas, time, light, accent)
        else drawSegmented(canvas, time, light, accent)
        return bitmap
    }

    private fun drawOutlineSolid(canvas: Canvas, time: String, solid: Int, outline: Int) {
        val digits = time.toCharArray()
        val positions = floatArrayOf(275f, 430f, 625f, 780f)
        digits.forEachIndexed { index, ch ->
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 245f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
                strokeWidth = 5f
                if (index < 2) {
                    color = outline
                    style = Paint.Style.STROKE
                } else {
                    color = solid
                    style = Paint.Style.FILL
                }
            }
            canvas.drawText(ch.toString(), positions[index], 315f, paint)
        }
    }

    private fun drawSegmented(canvas: Canvas, time: String, solid: Int, accent: Int) {
        val chars = time.toCharArray()
        val xPositions = floatArrayOf(275f, 430f, 625f, 780f)
        chars.forEachIndexed { i, ch ->
            drawSplitDigit(canvas, ch.digitToInt(), xPositions[i], 240f, if (i < 2) accent else solid)
        }
    }

    private fun drawSplitDigit(canvas: Canvas, digit: Int, cx: Float, cy: Float, color: Int) {
        // Seven-segment-inspired glyph with small gaps, styled like the segmented iOS reference.
        val segments = mapOf(
            0 to listOf(0,1,2,3,4,5), 1 to listOf(1,2), 2 to listOf(0,1,6,4,3),
            3 to listOf(0,1,6,2,3), 4 to listOf(5,6,1,2), 5 to listOf(0,5,6,2,3),
            6 to listOf(0,5,6,4,2,3), 7 to listOf(0,1,2), 8 to listOf(0,1,2,3,4,5,6),
            9 to listOf(0,1,2,3,5,6)
        )
        val active = segments[digit] ?: emptyList()
        val w = 112f
        val h = 218f
        val t = 27f
        val rects = listOf(
            RectF(cx-w/2+t/2, cy-h/2, cx+w/2-t/2, cy-h/2+t),
            RectF(cx+w/2-t, cy-h/2+t/2, cx+w/2, cy-t/2),
            RectF(cx+w/2-t, cy+t/2, cx+w/2, cy+h/2-t/2),
            RectF(cx-w/2+t/2, cy+h/2-t, cx+w/2-t/2, cy+h/2),
            RectF(cx-w/2, cy+t/2, cx-w/2+t, cy+h/2-t/2),
            RectF(cx-w/2, cy-h/2+t/2, cx-w/2+t, cy-t/2),
            RectF(cx-w/2+t/2, cy-t/2, cx+w/2-t/2, cy+t/2)
        )
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.FILL }
        active.forEach { index ->
            val r = RectF(rects[index])
            r.inset(3f, 3f)
            canvas.drawRoundRect(r, 9f, 9f, paint)
        }
    }
}
