package com.example.util.simpletimetracker.feature_dialogs.colorSelection

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_dialogs.colorSelection.model.HSVUpdate
import com.example.util.simpletimetracker.feature_dialogs.colorSelection.model.RGBUpdate
import javax.inject.Inject

class ColorSelectionViewModel @Inject constructor() : ViewModel() {

    val colorData: LiveData<ColorSelectionViewData> by lazy { MutableLiveData(loadColorData()) }
    val colorSelected: LiveData<String> = MutableLiveData()

    private var colorHue: Float = 0f // 0..360
    private var colorSaturation: Float = 1f // 0..1
    private var colorValue: Float = 1f // 0..1

    fun onHueChanged(hue: Float) {
        colorHue = hue.coerceIn(0f, 360f)
        updateColorData()
    }

    fun onColorChanged(saturation: Float, value: Float) {
        colorSaturation = saturation.coerceIn(0f, 1f)
        colorValue = value.coerceIn(0f, 1f)
        updateColorData()
    }

    fun onRGBChanged(colorString: String, update: RGBUpdate) {
        val colorInt = floatArrayOf(colorHue, colorSaturation, colorValue)
            .let(Color::HSVToColor)
        val currentRed = Color.red(colorInt)
        val currentGreen = Color.green(colorInt)
        val currentBlue = Color.blue(colorInt)
        val newColor = colorString.toIntOrNull().orZero().coerceIn(0, 255)

        val hsv = FloatArray(3)
        Color.RGBToHSV(
            if (update == RGBUpdate.R) newColor else currentRed,
            if (update == RGBUpdate.G) newColor else currentGreen,
            if (update == RGBUpdate.B) newColor else currentBlue,
            hsv
        )

        colorHue = hsv[0]
        colorSaturation = hsv[1]
        colorValue = hsv[2]

        updateColorData()
    }

    fun onHSVChanged(colorString: String, update: HSVUpdate) {
        colorString.toIntOrNull().orZero().apply {
            when (update) {
                HSVUpdate.H -> coerceIn(0, 360).let {
                    colorHue = it.toFloat()
                }
                HSVUpdate.S -> coerceIn(0, 100).let {
                    colorSaturation = it.toFloat() / 100f
                }
                HSVUpdate.V -> coerceIn(0, 100).let {
                    colorValue = it.toFloat() / 100f
                }
            }
        }

        updateColorData()
    }

    fun onSaveClick() {
        // TODO format color to HEX
        colorSelected.set("")
    }

    private fun updateColorData() {
        val data = loadColorData()
        colorData.set(data)
    }

    private fun loadColorData(): ColorSelectionViewData {
        val colorInt = floatArrayOf(colorHue, colorSaturation, colorValue)
            .let(Color::HSVToColor)

        return ColorSelectionViewData(
            selectedColor = colorInt,
            colorHue = colorHue,
            colorSaturation = colorSaturation,
            colorValue = colorValue,
            colorRedString = Color.red(colorInt).toString(),
            colorGreenString = Color.green(colorInt).toString(),
            colorBlueString = Color.blue(colorInt).toString(),
            colorHueString = colorHue.toInt().toString(),
            colorSaturationString = (colorSaturation * 100).toInt().toString(),
            colorValueString = (colorValue * 100).toInt().toString(),
        )
    }
}