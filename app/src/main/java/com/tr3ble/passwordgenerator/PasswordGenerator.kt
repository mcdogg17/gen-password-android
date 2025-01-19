package com.tr3ble.passwordgenerator

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.slider.Slider
import com.tr3ble.passwordgenerator.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val random = Random
    private var useNumbers = false
    private var useSpecialChars = false
    private var generatePin = false
    private var passwordLength = 8
    private var isGenerating = false
    private lateinit var sharedPreferences: SharedPreferences
    private val passwordHistory = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        setThemeBasedOnPreference()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.passwordLengthSeekBar.addOnChangeListener { _, value, _ ->
            passwordLength = value.toInt()
        }

        binding.useNumbersSwitch.setOnCheckedChangeListener { _, isChecked ->
            useNumbers = isChecked
        }

        binding.useSpecialCharsSwitch.setOnCheckedChangeListener { _, isChecked ->
            useSpecialChars = isChecked
        }

        binding.darkModeSwitch.isChecked = isDarkModeEnabled()
        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleDarkMode(isChecked)
        }

        binding.passwordTextView.addTextChangedListener {
            binding.copyButton.visibility = if ((it?.length ?: 0) >= 10) View.VISIBLE else View.INVISIBLE
        }

        binding.generateButton.setOnClickListener {
            generatePassword()
        }

        binding.copyButton.setOnClickListener {
            val clipboard = ContextCompat.getSystemService(applicationContext, ClipboardManager::class.java)
            val clip = ClipData.newPlainText("Пароль", binding.passwordTextView.text.toString())
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(this, "Пароль скопирован в буфер", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generatePassword() {
        if (isGenerating) return
        isGenerating = true

        val password = if (generatePin) {
            (1..passwordLength).map { random.nextInt(0, 10) }.joinToString("")
        } else {
            val ranges = mutableListOf('a'..'z', 'A'..'Z')
            if (useNumbers) ranges.add('0'..'9')
            if (useSpecialChars) ranges.add('!'..'/')
            (1..passwordLength).map { ranges.random().random() }.joinToString("")
        }

        updatePasswordHistory(password)
        binding.passwordTextView.text = "Пароль: $password"
        updateStrengthIndicator(password)
        isGenerating = false
    }

    private fun updatePasswordHistory(password: String) {
        if (passwordHistory.size >= 5) passwordHistory.removeAt(0)
        passwordHistory.add(password)
        binding.passwordHistoryTextView.text = passwordHistory.joinToString("\n")
    }

    private fun updateStrengthIndicator(password: String) {
        val strength = when {
            password.length >= 12 && password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() } -> "Сильный"
            password.length >= 10 -> "Средний"
            else -> "Слабый"
        }
        binding.passwordStrengthTextView.text = "Надежность: $strength"
    }

    private fun isDarkModeEnabled(): Boolean {
        return sharedPreferences.getBoolean("dark_mode", false)
    }

    private fun toggleDarkMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("dark_mode", enabled).apply()
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        recreate()
    }

    private fun setThemeBasedOnPreference() {
        if (isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
