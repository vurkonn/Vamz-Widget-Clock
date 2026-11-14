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
import kotlin.math.max

object ClockRenderer {
    const val PREFS = "clock_widget_prefs"
    const val KEY_MODEL = "model"
    const val KEY_BG = "background"
    const val KEY_BG_COLOR = "bg_color"
    const val KEY_BG_URI = "bg_uri"
    const val KEY_OUTLINE_COLOR = "outline_color"
    const val KEY_SOLID_COLOR = "solid_color"
    const val KEY_FRAME_COLOR = "frame_color"
    const val KEY_DATE_COLOR = "date_color"
    const val KEY_OUTLINE_WIDTH = "outline_width"
    const val KEY_SOLID_WIDTH = "solid_width"
    const val KEY_DIGIT_SIZE = "digit_size"
    const val KEY_SPACING = "digit_spacing"
    const val KEY_DIGIT_SCALE = "digit_scale"
    const val KEY_DIGIT_Y = "digit_y"
    const val KEY_DIGIT_X = "digit_x"
    const val KEY_FRAME_WIDTH = "frame_width"
    const val KEY_FRAME_RADIUS = "frame_radius"
    const val KEY_FRAME_INSET = "frame_inset"
    const val KEY_SHOW_DATE = "show_date"
    const val KEY_SHOW_FRAME = "show_frame"
    const val KEY_SHOW_COLON = "show_colon"
    const val KEY_TIME_FORMAT = "time_format"
    const val KEY_FONT = "font"
    const val KEY_FONT_WEIGHT = "font_weight"
    const val KEY_DATE_FORMAT = "date_format"
    const val KEY_DATE_SIZE = "date_size"
    const val KEY_DATE_Y = "date_y"
    const val KEY_DATE_ALIGN = "date_align"
    const val KEY_OUTLINE_DIGITS = "outline_digits"

    // Android system typefaces: broad selection without downloading external font files.
    val fontNames = listOf(
        "Roboto Thin", "Roboto Light", "Roboto Regular", "Roboto Medium", "Roboto Bold", "Roboto Black",
        "Roboto Condensed Light", "Roboto Condensed", "Roboto Condensed Medium", "Roboto Condensed Bold",
        "Roboto Condensed Black", "Roboto Rounded", "Sans Serif", "Sans Serif Small Caps",
        "Serif", "Serif Monospace", "Monospace", "Casual", "Cursive",
        "Default", "Default Bold", "Sans Serif Thin", "Sans Serif Light", "Sans Serif Medium",
        "Sans Serif Black", "Sans Serif Condensed", "Sans Serif Condensed Bold", "Sans Serif Rounded"
    )
    val fontFamilies = listOf(
        "sans-serif-thin", "sans-serif-light", "sans-serif", "sans-serif-medium", "sans-serif", "sans-serif-black",
        "sans-serif-condensed-light", "sans-serif-condensed", "sans-serif-condensed-medium", "sans-serif-condensed",
        "sans-serif-condensed", "sans-serif-rounded", "sans-serif", "sans-serif-smallcaps",
        "serif", "serif-monospace", "monospace", "casual", "cursive",
        "default", "default", "sans-serif-thin", "sans-serif-light", "sans-serif-medium",
        "sans-serif-black", "sans-serif-condensed", "sans-serif-condensed", "sans-serif-rounded"
    )
    val fontWeightNames = listOf("Tipis", "Normal", "Sedang", "Tebal", "Ekstra tebal")
    val fontWeightValues = listOf(Typeface.NORMAL, Typeface.NORMAL, Typeface.BOLD, Typeface.BOLD, Typeface.BOLD)
    val dateFormats = listOf("EEE, MMM d", "dd MMM yyyy", "EEEE, dd MMMM", "dd/MM/yyyy")

    fun render(context: Context): Bitmap {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val width = 1000
        val height = 420
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgMode = p.getInt(KEY_BG, 0)
        when (bgMode) {
            0 -> canvas.drawColor(Color.TRANSPARENT)
            1 -> canvas.drawColor(p.getInt(KEY_BG_COLOR, Color.rgb(10, 10, 13)))
            2 -> {
                val uriText = p.getString(KEY_BG_URI, null)
                val image = try { if (uriText != null) context.contentResolver.openInputStream(Uri.parse(uriText))?.use { BitmapFactory.decodeStream(it) } else null } catch (_: Exception) { null }
                if (image != null) {
                    val scale = max(width.toFloat() / image.width, height.toFloat() / image.height)
                    val w = image.width * scale
                    val h = image.height * scale
                    val dst = RectF((width-w)/2f, (height-h)/2f, (width+w)/2f, (height+h)/2f)
                    canvas.drawBitmap(image, null, dst, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                    image.recycle()
                } else canvas.drawColor(p.getInt(KEY_BG_COLOR, Color.rgb(10, 10, 13)))
            }
        }

        val outlineColor = p.getInt(KEY_OUTLINE_COLOR, Color.rgb(225, 222, 255))
        val solidColor = p.getInt(KEY_SOLID_COLOR, Color.WHITE)
        val frameColor = p.getInt(KEY_FRAME_COLOR, Color.rgb(173, 166, 235))
        val dateColor = p.getInt(KEY_DATE_COLOR, solidColor)
        val showDate = p.getBoolean(KEY_SHOW_DATE, true)
        val showFrame = p.getBoolean(KEY_SHOW_FRAME, true)
        val showColon = p.getBoolean(KEY_SHOW_COLON, false)
        val outlineWidth = p.getInt(KEY_OUTLINE_WIDTH, 5).toFloat().coerceIn(1f, 30f)
        val solidWidth = p.getInt(KEY_SOLID_WIDTH, 0).toFloat().coerceIn(0f, 20f)
        val digitSize = p.getInt(KEY_DIGIT_SIZE, 245).toFloat().coerceIn(100f, 320f)
        val spacing = p.getInt(KEY_SPACING, 155).toFloat().coerceIn(90f, 240f)
        val digitScale = p.getInt(KEY_DIGIT_SCALE, 100) / 100f
        val digitY = p.getInt(KEY_DIGIT_Y, 315).toFloat().coerceIn(120f, 390f)
        val digitX = p.getInt(KEY_DIGIT_X, 500).toFloat().coerceIn(250f, 750f)
        val frameWidth = p.getInt(KEY_FRAME_WIDTH, 7).toFloat().coerceIn(0f, 24f)
        val frameRadius = p.getInt(KEY_FRAME_RADIUS, 42).toFloat().coerceIn(0f, 140f)
        val frameInset = p.getInt(KEY_FRAME_INSET, 45).toFloat().coerceIn(0f, 160f)
        val dateSize = p.getInt(KEY_DATE_SIZE, 38).toFloat().coerceIn(16f, 70f)
        val dateY = p.getInt(KEY_DATE_Y, 62).toFloat().coerceIn(15f, 120f)
        val fontIndex = p.getInt(KEY_FONT, 0).coerceIn(0, fontFamilies.lastIndex)
        val dateAlign = p.getInt(KEY_DATE_ALIGN, 1).coerceIn(0, 2)
        val outlineDigits = p.getInt(KEY_OUTLINE_DIGITS, 2).coerceIn(0, 4)

        if (showDate) {
            val format = dateFormats[p.getInt(KEY_DATE_FORMAT, 0).coerceIn(0, dateFormats.lastIndex)]
            val date = SimpleDateFormat(format, Locale.getDefault()).format(Date()).uppercase(Locale.getDefault())
            val align = when (dateAlign) { 0 -> Paint.Align.LEFT; 2 -> Paint.Align.RIGHT; else -> Paint.Align.CENTER }
            val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = dateColor
                textSize = dateSize
                textAlign = align
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            }
            val x = when (dateAlign) { 0 -> 60f; 2 -> 940f; else -> 500f }
            canvas.drawText(date, x, dateY, datePaint)
        }

        if (showFrame && frameWidth > 0f) {
            val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = frameColor
                style = Paint.Style.STROKE
                strokeWidth = frameWidth
            }
            canvas.drawRoundRect(RectF(frameInset, 95f, width - frameInset, 385f), frameRadius, frameRadius, framePaint)
        }

        val format = if (p.getInt(KEY_TIME_FORMAT, 0) == 1) "hhmm" else "HHmm"
        val rawTime = SimpleDateFormat(format, Locale.getDefault()).format(Date())
        val time = if (showColon) rawTime.substring(0, 2) + ":" + rawTime.substring(2) else rawTime
        val gap = spacing
        val positions = if (showColon) {
            floatArrayOf(digitX - gap * 2.05f, digitX - gap * 1.05f, digitX, digitX + gap * 1.05f, digitX + gap * 2.05f)
        } else {
            floatArrayOf(digitX - gap * 1.5f, digitX - gap * .5f, digitX + gap * .5f, digitX + gap * 1.5f)
        }
        val weightIndex = p.getInt(KEY_FONT_WEIGHT, 3).coerceIn(0, fontWeightNames.lastIndex)
        val typefaceStyle = when (weightIndex) {
            0 -> Typeface.NORMAL
            1 -> Typeface.NORMAL
            2 -> Typeface.BOLD
            3 -> Typeface.BOLD
            else -> Typeface.BOLD
        }
        val typeface = Typeface.create(fontFamilies[fontIndex], typefaceStyle)
        var digitIndex = 0
        time.forEach { ch ->
            if (ch == ':') {
                val colonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = solidColor; textSize = digitSize * .62f; textAlign = Paint.Align.CENTER; this.typeface = typeface }
                canvas.drawText(":", positions[2], digitY - digitSize * .28f, colonPaint)
            } else {
                val outline = digitIndex < outlineDigits
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                    textSize = digitSize
                    textAlign = Paint.Align.CENTER
                    this.typeface = typeface
                    strokeJoin = Paint.Join.ROUND
                    strokeCap = Paint.Cap.ROUND
                    textScaleX = digitScale
                    if (outline) {
                        color = outlineColor
                        style = Paint.Style.STROKE
                        strokeWidth = outlineWidth
                    } else {
                        color = solidColor
                        style = if (solidWidth > 0f) Paint.Style.FILL_AND_STROKE else Paint.Style.FILL
                        strokeWidth = solidWidth
                    }
                }
                val posIndex = if (showColon && digitIndex >= 2) digitIndex + 1 else digitIndex
                canvas.drawText(ch.toString(), positions[posIndex], digitY, paint)
                digitIndex++
            }
        }
        return bitmap
    }
}
