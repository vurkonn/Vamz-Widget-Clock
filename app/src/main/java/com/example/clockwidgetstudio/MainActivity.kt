package com.example.clockwidgetstudio

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {
    private val pickImage = 501
    private lateinit var page: LinearLayout
    private lateinit var bgSpinner: Spinner
    private lateinit var bgHex: EditText
    private lateinit var outlineHex: EditText
    private lateinit var solidHex: EditText
    private lateinit var frameHex: EditText
    private val sliders = mutableListOf<Pair<String, SeekBar>>()
    private lateinit var showDate: Switch
    private lateinit var showFrame: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences(ClockRenderer.PREFS, MODE_PRIVATE)
        page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 28, 24, 28)
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
            text = "Atur bentuk jam sesuka kamu. Perubahan diterapkan ke widget homescreen."
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, 8, 0, 18)
        })

        section("BACKGROUND")
        bgSpinner = Spinner(this)
        bgSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Transparan", "Warna solid", "Foto sendiri"))
        bgSpinner.setSelection(prefs.getInt(ClockRenderer.KEY_BG, 1))
        page.addView(bgSpinner)
        bgHex = editColor(prefs.getInt(ClockRenderer.KEY_BG_COLOR, Color.rgb(10, 10, 13)))
        page.addView(bgHex)
        page.addView(button("PILIH FOTO BACKGROUND") { openImagePicker() })

        section("MODEL TAMPILAN")
        showDate = Switch(this).apply {
            text = "Tampilkan tanggal di atas jam"
            setTextColor(Color.WHITE)
            isChecked = prefs.getBoolean(ClockRenderer.KEY_SHOW_DATE, true)
        }
        page.addView(showDate)
        showFrame = Switch(this).apply {
            text = "Tampilkan bingkai rounded"
            setTextColor(Color.WHITE)
            isChecked = prefs.getBoolean(ClockRenderer.KEY_SHOW_FRAME, true)
        }
        page.addView(showFrame)

        section("KETEBALAN & PROPORSI")
        addSlider("Ketebalan outline", ClockRenderer.KEY_OUTLINE_WIDTH, 1, 24, 5, prefs.getInt(ClockRenderer.KEY_OUTLINE_WIDTH, 5))
        addSlider("Ketebalan angka solid", ClockRenderer.KEY_SOLID_WIDTH, 0, 24, 2, prefs.getInt(ClockRenderer.KEY_SOLID_WIDTH, 2))
        addSlider("Ukuran angka", ClockRenderer.KEY_DIGIT_SIZE, 150, 300, 245, prefs.getInt(ClockRenderer.KEY_DIGIT_SIZE, 245))
        addSlider("Jarak antarangka", ClockRenderer.KEY_SPACING, 110, 220, 155, prefs.getInt(ClockRenderer.KEY_SPACING, 155))
        addSlider("Ketebalan bingkai", ClockRenderer.KEY_FRAME_WIDTH, 1, 20, 7, prefs.getInt(ClockRenderer.KEY_FRAME_WIDTH, 7))
        addSlider("Radius sudut bingkai", ClockRenderer.KEY_FRAME_RADIUS, 0, 100, 42, prefs.getInt(ClockRenderer.KEY_FRAME_RADIUS, 42))

        section("WARNA (HEX)")
        page.addView(label("Warna outline — contoh #E1DEFF"))
        outlineHex = editColor(prefs.getInt(ClockRenderer.KEY_OUTLINE_COLOR, Color.rgb(225, 222, 255)))
        page.addView(outlineHex)
        page.addView(label("Warna angka solid — contoh #FFFFFF"))
        solidHex = editColor(prefs.getInt(ClockRenderer.KEY_SOLID_COLOR, Color.WHITE))
        page.addView(solidHex)
        page.addView(label("Warna bingkai — contoh #ADA6EB"))
        frameHex = editColor(prefs.getInt(ClockRenderer.KEY_FRAME_COLOR, Color.rgb(173, 166, 235)))
        page.addView(frameHex)

        page.addView(button("SIMPAN & PERBARUI WIDGET") {
            val edit = prefs.edit()
                .putInt(ClockRenderer.KEY_BG, bgSpinner.selectedItemPosition)
                .putInt(ClockRenderer.KEY_BG_COLOR, parseColor(bgHex.text.toString(), Color.rgb(10, 10, 13)))
                .putInt(ClockRenderer.KEY_OUTLINE_COLOR, parseColor(outlineHex.text.toString(), Color.rgb(225, 222, 255)))
                .putInt(ClockRenderer.KEY_SOLID_COLOR, parseColor(solidHex.text.toString(), Color.WHITE))
                .putInt(ClockRenderer.KEY_FRAME_COLOR, parseColor(frameHex.text.toString(), Color.rgb(173, 166, 235)))
                .putBoolean(ClockRenderer.KEY_SHOW_DATE, showDate.isChecked)
                .putBoolean(ClockRenderer.KEY_SHOW_FRAME, showFrame.isChecked)
            sliders.forEach { (key, seek) -> edit.putInt(key, seek.progress + sliderMin(key)) }
            edit.apply()
            ClockWidgetProvider.updateAll(this)
            android.widget.Toast.makeText(this, "Pengaturan disimpan. Widget diperbarui.", android.widget.Toast.LENGTH_SHORT).show()
        }.also { it.setBackgroundColor(Color.rgb(100, 83, 210)) })
        page.addView(TextView(this).apply {
            text = "Catatan: tambahkan widget Clock Widget Studio ke homescreen. Jika sudah terpasang, tekan simpan untuk memperbarui tampilannya."
            textSize = 12f
            setTextColor(Color.LTGRAY)
            setPadding(0, 18, 0, 24)
        })
        setContentView(scroll)
    }

    private fun sliderMin(key: String): Int = when (key) {
        ClockRenderer.KEY_OUTLINE_WIDTH, ClockRenderer.KEY_FRAME_WIDTH -> 1
        ClockRenderer.KEY_SOLID_WIDTH -> 0
        ClockRenderer.KEY_DIGIT_SIZE -> 150
        ClockRenderer.KEY_SPACING -> 110
        else -> 0
    }

    private fun addSlider(title: String, key: String, min: Int, max: Int, default: Int, value: Int) {
        page.addView(label("$title: $value"))
        val seek = SeekBar(this).apply {
            this.max = max - min
            progress = (value - min).coerceIn(0, this.max)
        }
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                (bar?.tag as? TextView)?.text = "$title: ${progress + min}"
            }
            override fun onStartTrackingTouch(bar: SeekBar?) {}
            override fun onStopTrackingTouch(bar: SeekBar?) {}
        })
        val titleView = page.getChildAt(page.childCount - 1) as TextView
        seek.tag = titleView
        page.addView(seek)
        sliders.add(key to seek)
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
        setPadding(0, 10, 0, 4)
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
        lp.topMargin = 10
        layoutParams = lp
    }

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
            android.widget.Toast.makeText(this, "Foto dipilih. Tekan Simpan untuk menerapkan.", android.widget.Toast.LENGTH_LONG).show()
        }
    }
}
