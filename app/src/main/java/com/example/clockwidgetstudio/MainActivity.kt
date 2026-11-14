package com.example.clockwidgetstudio

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var modelSpinner: Spinner
    private lateinit var backgroundSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(ClockRenderer.PREFS, MODE_PRIVATE)
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 36, 28, 28)
            setBackgroundColor(Color.rgb(17, 17, 22))
        }

        val heading = TextView(this).apply {
            text = "CLOCK WIDGET STUDIO"
            textSize = 22f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL
        }
        page.addView(heading)

        val subtitle = TextView(this).apply {
            text = "Pilih gaya jam dan background widget."
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, 8, 0, 24)
        }
        page.addView(subtitle)

        page.addView(label("MODEL JAM"))
        modelSpinner = Spinner(this)
        modelSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Outline + Solid", "Segmented iOS"))
        modelSpinner.setSelection(prefs.getInt(ClockRenderer.KEY_MODEL, 0))
        page.addView(modelSpinner)

        page.addView(label("BACKGROUND"))
        backgroundSpinner = Spinner(this)
        backgroundSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Transparan", "Ungu iOS", "Hitam", "Putih"))
        backgroundSpinner.setSelection(prefs.getInt(ClockRenderer.KEY_BG, 1))
        page.addView(backgroundSpinner)

        val save = Button(this).apply {
            text = "SIMPAN & PERBARUI WIDGET"
            setOnClickListener {
                prefs.edit()
                    .putInt(ClockRenderer.KEY_MODEL, modelSpinner.selectedItemPosition)
                    .putInt(ClockRenderer.KEY_BG, backgroundSpinner.selectedItemPosition)
                    .apply()
                ClockWidgetProvider.updateAll(this@MainActivity)
                finish()
            }
        }
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 28 }
        page.addView(save, params)

        val hint = TextView(this).apply {
            text = "Setelah menyimpan, tambahkan widget “Clock Widget Studio” dari menu widget launcher."
            textSize = 13f
            setTextColor(Color.LTGRAY)
            setPadding(0, 24, 0, 0)
        }
        page.addView(hint)
        setContentView(page)
    }

    private fun label(value: String) = TextView(this).apply {
        text = value
        textSize = 12f
        setTextColor(Color.rgb(180, 175, 235))
        setPadding(0, 20, 0, 6)
    }
}
