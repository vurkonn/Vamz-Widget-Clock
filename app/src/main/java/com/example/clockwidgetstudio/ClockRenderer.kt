package com.example.clockwidgetstudio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ClockRenderer {
    const val PREFS = "clock_widget_prefs"
    const val KEY_MODEL = "model"
    const val KEY_BG = "background"
    const val KEY_BG_COLOR = "bg_color"
    const val KEY_BG_URI = "bg_uri"
    const val KEY_OUTLINE_COLOR = "outline_color"
    const val KEY_SOLID_COLOR = "solid_color"
    const val KEY_FRAME_COLOR = "frame_color"
    const val KEY_OUTLINE_WIDTH = "outline_width"
    const val KEY_SOLID_WIDTH = "solid_width"
    const val KEY_DIGIT_SIZE = "digit_size"
    const val KEY_SPACING = "digit_spacing"
    const val KEY_FRAME_WIDTH = "frame_width"
    const val KEY_FRAME_RADIUS = "frame_radius"
    const val KEY_SHOW_DATE = "show_date"
    const val KEY_SHOW_FRAME = "show_frame"

    fun render(context: Context): Bitmap {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val bitmap = Bitmap.createBitmap(1000, 420, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgMode = p.getInt(KEY_BG, 1)
        when (bgMode) {
            0 -> canvas.drawColor(Color.TRANSPARENT)
            1 -> canvas.drawColor(p.getInt(KEY_BG_COLOR, Color.rgb(10, 10, 13)))
            2 -> {
                val uriText = p.getString(KEY_BG_URI, null)
                val image = try { if (uriText != null) context.contentResolver.openInputStream(Uri.parse(uriText))?.use { BitmapFactory.decodeStream(it) } else null } catch (_: Exception) { null }
                if (image != null) {
                    val scale = maxOf(1000f / image.width, 420f / image.height)
                    val w = image.width * scale
                    val h = image.height * scale
                    val src = RectF((1000f-w)/2f, (420f-h)/2f, (1000f+w)/2f, (420f+h)/2f)
                    canvas.drawBitmap(image, null, src, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                    image.recycle()
                } else canvas.drawColor(p.getInt(KEY_BG_COLOR, Color.rgb(10, 10, 13)))
            }
        }

        val outlineColor = p.getInt(KEY_OUTLINE_COLOR, Color.rgb(225, 222, 255))
        val solidColor = p.getInt(KEY_SOLID_COLOR, Color.WHITE)
        val frameColor = p.getInt(KEY_FRAME_COLOR, outlineColor)
        val showDate = p.getBoolean(KEY_SHOW_DATE, true)
        val showFrame = p.getBoolean(KEY_SHOW_FRAME, true)
        val outlineWidth = p.getInt(KEY_OUTLINE_WIDTH, 5).toFloat().coerceIn(1f, 24f)
        val solidWidth = p.getInt(KEY_SOLID_WIDTH, 2).toFloat().coerceIn(0f, 24f)
        val digitSize = p.getInt(KEY_DIGIT_SIZE, 245).toFloat().coerceIn(150f, 300f)
        val spacing = p.getInt(KEY_SPACING, 155).toFloat().coerceIn(110f, 220f)
        val frameWidth = p.getInt(KEY_FRAME_WIDTH, 7).toFloat().coerceIn(1f, 20f)
        val frameRadius = p.getInt(KEY_FRAME_RADIUS, 42).toFloat().coerceIn(0f, 100f)

        if (showDate) {
            val date = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date()).uppercase(Locale.getDefault())
            val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = solidColor
                textSize = 38f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            }
            canvas.drawText(date, 500f, 62f, datePaint)
        }

        if (showFrame) {
            val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = frameColor
                style = Paint.Style.STROKE
                strokeWidth = frameWidth
            }
            canvas.drawRoundRect(RectF(45f, 95f, 955f, 385f), frameRadius, frameRadius, framePaint)
        }

        val time = SimpleDateFormat("HHmm", Locale.getDefault()).format(Date())
        val center = 500f
        val gap = spacing
        val positions = floatArrayOf(center - gap * 1.5f, center - gap * .5f, center + gap * .5f, center + gap * 1.5f)
        val baseline = if (showDate) 315f else 285f
        val typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        time.forEachIndexed { index, ch ->
            val outline = index < 2
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                textSize = digitSize
                textAlign = Paint.Align.CENTER
                this.typeface = typeface
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
                if (outline) {
                    color = outlineColor
                    style = Paint.Style.STROKE
                    strokeWidth = outlineWidth
                } else {
                    color = solidColor
                    style = Paint.Style.FILL_AND_STROKE
                    strokeWidth = solidWidth
                }
            }
            canvas.drawText(ch.toString(), positions[index], baseline, paint)
        }
        return bitmap
    }
}
