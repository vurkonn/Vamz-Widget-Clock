package com.example.clockwidgetstudio

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val pickImage = 501
    private lateinit var page: LinearLayout
    private lateinit var preview: ImageView
    private lateinit var bgSpinner: Spinner
    private lateinit var fontSpinner: Spinner
    private lateinit var fontWeightSpinner: Spinner
    private lateinit var dateFormatSpinner: Spinner
    private lateinit var dateAlignSpinner: Spinner
    private lateinit var timeFormatSpinner: Spinner
    private lateinit var bgHex: EditText
    private lateinit var outlineHex: EditText
    private lateinit var solidHex: EditText
    private lateinit var frameHex: EditText
    private lateinit var dateHex: EditText
    private val sliders = mutableListOf<Triple<String, SeekBar, Int>>()
    private lateinit var showDate: Switch
    private lateinit var showFrame: Switch
    private lateinit var showColon: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences(ClockRenderer.PREFS, MODE_PRIVATE)
        page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 24, 22, 28)
            setBackgroundColor(Color.rgb(16, 16, 21))
        }
        val scroll = ScrollView(this).apply { addView(page) }
        page.addView(TextView(this).apply {
            text = "CLOCK WIDGET STUDIO"
            textSize = 23f
            setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        })
        page.addView(TextView(this).apply {
            text = "Editor jam lengkap — atur bentuk, posisi, warna, dan detail angka."
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, 7, 0, 14)
        })

        section("LIVE PREVIEW")
        preview = ImageView(this).apply {
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.rgb(28, 28, 35))
            contentDescription = "Pratinjau jam"
        }
        page.addView(preview, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(168)))
        page.addView(button("LIHAT PREVIEW TERBARU") { refreshPreview() })

        section("BACKGROUND")
        bgSpinner = spinner(listOf("Transparan", "Warna solid", "Foto sendiri"), prefs.getInt(ClockRenderer.KEY_BG, 0))
        page.addView(bgSpinner)
        page.addView(label("Warna background (HEX)"))
        bgHex = editColor(prefs.getInt(ClockRenderer.KEY_BG_COLOR, Color.rgb(10, 10, 13)))
        page.addView(bgHex)
        page.addView(button("PILIH FOTO DARI GALERI") { openImagePicker() })

        section("MODEL & FORMAT JAM")
        page.addView(label("Gaya font angka"))
        fontSpinner = spinner(ClockRenderer.fontNames, prefs.getInt(ClockRenderer.KEY_FONT, 0))
        page.addView(fontSpinner)
        page.addView(label("Ketebalan / weight font"))
        fontWeightSpinner = spinner(ClockRenderer.fontWeightNames, prefs.getInt(ClockRenderer.KEY_FONT_WEIGHT, 3))
        page.addView(fontWeightSpinner)
        page.addView(TextView(this).apply {
            text = "Pilihan font memakai keluarga font bawaan Android agar tetap ringan dan tidak perlu koneksi internet. Sebagian nama dapat terlihat mirip karena Android menyesuaikan font yang tersedia di perangkat."
            textSize = 11f
            setTextColor(Color.LTGRAY)
            setPadding(0, 4, 0, 8)
        })
        page.addView(label("Format waktu"))
        timeFormatSpinner = spinner(listOf("24 jam (15:45)", "12 jam (03:45)"), prefs.getInt(ClockRenderer.KEY_TIME_FORMAT, 0))
        page.addView(timeFormatSpinner)
        showColon = toggle("Tampilkan titik dua (:", prefs.getBoolean(ClockRenderer.KEY_SHOW_COLON, false))
        page.addView(showColon)
        showDate = toggle("Tampilkan tanggal", prefs.getBoolean(ClockRenderer.KEY_SHOW_DATE, true))
        page.addView(showDate)
        page.addView(label("Format tanggal"))
        dateFormatSpinner = spinner(ClockRenderer.dateFormats, prefs.getInt(ClockRenderer.KEY_DATE_FORMAT, 0))
        page.addView(dateFormatSpinner)
        page.addView(label("Posisi teks tanggal"))
        dateAlignSpinner = spinner(listOf("Kiri", "Tengah", "Kanan"), prefs.getInt(ClockRenderer.KEY_DATE_ALIGN, 1))
        page.addView(dateAlignSpinner)
        showFrame = toggle("Tampilkan bingkai rounded", prefs.getBoolean(ClockRenderer.KEY_SHOW_FRAME, true))
        page.addView(showFrame)

        section("ANGKA — BENTUK & PROPORSI")
        addSlider("Jumlah digit outline (0–4)", ClockRenderer.KEY_OUTLINE_DIGITS, 0, 4, 2, prefs.getInt(ClockRenderer.KEY_OUTLINE_DIGITS, 2))
        addSlider("Tebal garis outline", ClockRenderer.KEY_OUTLINE_WIDTH, 1, 30, 5, prefs.getInt(ClockRenderer.KEY_OUTLINE_WIDTH, 5))
        addSlider("Tebal tambahan angka solid", ClockRenderer.KEY_SOLID_WIDTH, 0, 20, 0, prefs.getInt(ClockRenderer.KEY_SOLID_WIDTH, 0))
        addSlider("Ukuran angka", ClockRenderer.KEY_DIGIT_SIZE, 100, 320, 245, prefs.getInt(ClockRenderer.KEY_DIGIT_SIZE, 245))
        addSlider("Lebar / stretch angka (%)", ClockRenderer.KEY_DIGIT_SCALE, 60, 150, 100, prefs.getInt(ClockRenderer.KEY_DIGIT_SCALE, 100))
        addSlider("Jarak antarangka", ClockRenderer.KEY_SPACING, 90, 240, 155, prefs.getInt(ClockRenderer.KEY_SPACING, 155))
        addSlider("Posisi jam horizontal", ClockRenderer.KEY_DIGIT_X, 250, 750, 500, prefs.getInt(ClockRenderer.KEY_DIGIT_X, 500))
        addSlider("Posisi jam vertikal", ClockRenderer.KEY_DIGIT_Y, 120, 390, 315, prefs.getInt(ClockRenderer.KEY_DIGIT_Y, 315))

        section("TANGGAL — UKURAN & POSISI")
        addSlider("Ukuran teks tanggal", ClockRenderer.KEY_DATE_SIZE, 16, 70, 38, prefs.getInt(ClockRenderer.KEY_DATE_SIZE, 38))
        addSlider("Posisi tanggal vertikal", ClockRenderer.KEY_DATE_Y, 15, 120, 62, prefs.getInt(ClockRenderer.KEY_DATE_Y, 62))

        section("BINGKAI — DETAIL")
        addSlider("Ketebalan bingkai", ClockRenderer.KEY_FRAME_WIDTH, 0, 24, 7, prefs.getInt(ClockRenderer.KEY_FRAME_WIDTH, 7))
        addSlider("Radius sudut", ClockRenderer.KEY_FRAME_RADIUS, 0, 140, 42, prefs.getInt(ClockRenderer.KEY_FRAME_RADIUS, 42))
        addSlider("Inset / jarak bingkai dari tepi", ClockRenderer.KEY_FRAME_INSET, 0, 160, 45, prefs.getInt(ClockRenderer.KEY_FRAME_INSET, 45))

        section("WARNA CUSTOM (HEX)")
        page.addView(label("Warna outline"))
        outlineHex = editColor(prefs.getInt(ClockRenderer.KEY_OUTLINE_COLOR, Color.rgb(225, 222, 255)))
        page.addView(outlineHex)
        page.addView(label("Warna angka solid"))
        solidHex = editColor(prefs.getInt(ClockRenderer.KEY_SOLID_COLOR, Color.WHITE))
        page.addView(solidHex)
        page.addView(label("Warna tanggal"))
        dateHex = editColor(prefs.getInt(ClockRenderer.KEY_DATE_COLOR, Color.WHITE))
        page.addView(dateHex)
        page.addView(label("Warna bingkai"))
        frameHex = editColor(prefs.getInt(ClockRenderer.KEY_FRAME_COLOR, Color.rgb(173, 166, 235)))
        page.addView(frameHex)

        page.addView(button("SIMPAN & PERBARUI WIDGET") {
            persistSettings()
            ClockWidgetProvider.updateAll(this)
            refreshPreview()
            Toast.makeText(this, "Pengaturan disimpan. Widget diperbarui.", Toast.LENGTH_SHORT).show()
        }.apply { setBackgroundColor(Color.rgb(100, 83, 210)); textSize = 15f })
        page.addView(TextView(this).apply {
            text = "Tips: atur slider, tekan Preview untuk melihat hasil, lalu Simpan. Jika widget belum muncul, tambahkan Clock Widget Studio dari daftar widget homescreen."
            textSize = 12f
            setTextColor(Color.LTGRAY)
            setPadding(0, 14, 0, 20)
        })
        setContentView(scroll)
        refreshPreview()
    }

    private fun persistSettings() {
        val prefs = getSharedPreferences(ClockRenderer.PREFS, MODE_PRIVATE)
        val edit = prefs.edit()
            .putInt(ClockRenderer.KEY_BG, bgSpinner.selectedItemPosition)
            .putInt(ClockRenderer.KEY_BG_COLOR, parseColor(bgHex.text.toString(), Color.rgb(10, 10, 13)))
            .putInt(ClockRenderer.KEY_OUTLINE_COLOR, parseColor(outlineHex.text.toString(), Color.rgb(225, 222, 255)))
            .putInt(ClockRenderer.KEY_SOLID_COLOR, parseColor(solidHex.text.toString(), Color.WHITE))
            .putInt(ClockRenderer.KEY_FRAME_COLOR, parseColor(frameHex.text.toString(), Color.rgb(173, 166, 235)))
            .putInt(ClockRenderer.KEY_DATE_COLOR, parseColor(dateHex.text.toString(), Color.WHITE))
            .putBoolean(ClockRenderer.KEY_SHOW_DATE, showDate.isChecked)
            .putBoolean(ClockRenderer.KEY_SHOW_FRAME, showFrame.isChecked)
            .putBoolean(ClockRenderer.KEY_SHOW_COLON, showColon.isChecked)
            .putInt(ClockRenderer.KEY_FONT, fontSpinner.selectedItemPosition)
            .putInt(ClockRenderer.KEY_FONT_WEIGHT, fontWeightSpinner.selectedItemPosition)
            .putInt(ClockRenderer.KEY_TIME_FORMAT, timeFormatSpinner.selectedItemPosition)
            .putInt(ClockRenderer.KEY_DATE_FORMAT, dateFormatSpinner.selectedItemPosition)
            .putInt(ClockRenderer.KEY_DATE_ALIGN, dateAlignSpinner.selectedItemPosition)
        sliders.forEach { (key, seek, min) -> edit.putInt(key, seek.progress + min) }
        edit.apply()
    }

    private fun refreshPreview() {
        if (!::preview.isInitialized) return
        persistSettings()
        val bitmap: Bitmap = ClockRenderer.render(this)
        preview.setImageBitmap(bitmap)
    }

    private fun addSlider(title: String, key: String, min: Int, max: Int, default: Int, value: Int) {
        val titleView = label("$title: $value")
        page.addView(titleView)
        val seek = SeekBar(this).apply {
            this.max = max - min
            progress = (value - min).coerceIn(0, this.max)
        }
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                titleView.text = "$title: ${progress + min}"
            }
            override fun onStartTrackingTouch(bar: SeekBar?) {}
            override fun onStopTrackingTouch(bar: SeekBar?) { refreshPreview() }
        })
        page.addView(seek)
        sliders.add(Triple(key, seek, min))
    }

    private fun section(text: String) {
        page.addView(TextView(this).apply {
            this.text = text
            textSize = 12f
            setTextColor(Color.rgb(183, 170, 255))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, 22, 0, 8)
        })
    }

    private fun label(text: String) = TextView(this).apply {
        this.text = text
        textSize = 13f
        setTextColor(Color.WHITE)
        setPadding(0, 9, 0, 3)
    }

    private fun toggle(text: String, checked: Boolean) = Switch(this).apply {
        this.text = text
        setTextColor(Color.WHITE)
        isChecked = checked
    }

    private fun spinner(items: List<String>, selected: Int) = Spinner(this).apply {
        adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, items)
        setSelection(selected.coerceIn(0, items.lastIndex))
    }

    private fun editColor(color: Int) = EditText(this).apply {
        setSingleLine(true)
        setText(String.format("#%06X", 0xFFFFFF and color))
        setTextColor(Color.WHITE)
        setHintTextColor(Color.LTGRAY)
        setHint("#FFFFFF")
        setPadding(12, 8, 12, 8)
        setBackgroundColor(Color.rgb(42, 42, 51))
    }

    private fun parseColor(value: String, fallback: Int): Int = try {
        Color.parseColor(if (value.startsWith("#")) value else "#$value")
    } catch (_: Exception) { fallback }

    private fun button(text: String, action: () -> Unit) = Button(this).apply {
        this.text = text
        setTextColor(Color.WHITE)
        setOnClickListener { action() }
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.topMargin = dp(8)
        layoutParams = lp
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(intent, pickImage)
    }

    @Deprecated("Deprecated in Android API, retained for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImage && resultCode == RESULT_OK) {
            val uri: Uri = data?.data ?: return
            try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            getSharedPreferences(ClockRenderer.PREFS, MODE_PRIVATE).edit()
                .putString(ClockRenderer.KEY_BG_URI, uri.toString()).putInt(ClockRenderer.KEY_BG, 2).apply()
            bgSpinner.setSelection(2)
            Toast.makeText(this, "Foto dipilih. Tekan Preview lalu Simpan.", Toast.LENGTH_LONG).show()
            refreshPreview()
        }
    }
}
