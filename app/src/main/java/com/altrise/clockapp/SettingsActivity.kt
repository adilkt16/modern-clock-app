package com.altrise.clockapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat

class SettingsActivity : Activity() {
    
    private lateinit var sharedPrefs: android.content.SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sharedPrefs = getSharedPreferences("ClockAppSettings", Context.MODE_PRIVATE)
        
        setupUI()
    }
    
    private fun setupUI() {
        // Main container with matching gradient
        val mainLayout = FrameLayout(this).apply {
            background = android.graphics.drawable.GradientDrawable().apply {
                orientation = android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM
                colors = intArrayOf(
                    Color.parseColor("#1A237E"), // Deep indigo
                    Color.parseColor("#3F51B5")  // Material indigo
                )
            }
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        
        // Determine status bar height for safe-area padding
        val statusBarHeight = resources.getIdentifier("status_bar_height", "dimen", "android").let { id ->
            if (id > 0) resources.getDimensionPixelSize(id) else 60
        }
        // ScrollView for content with top padding below status bar
        val scrollView = ScrollView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(40, statusBarHeight + 24, 40, 40)
        }
        
        // Content container
        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        // Header with back button (inside its own container for separation)
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_VERTICAL
        }
        
        // Back text button replacing icon for better alignment
        val backButton = TextView(this).apply {
            text = "Back"
            textSize = 18f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(20, 10, 20, 10)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 24, 0) }
            // Small rounded border
            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 20f * resources.displayMetrics.density
                setStroke((2 * resources.displayMetrics.density).toInt(), Color.parseColor("#31A82A"))
                setColor(Color.TRANSPARENT)
            }
            isClickable = true
            isFocusable = true
            setOnClickListener { finish() }
            contentDescription = "Go back"
        }
        
        // Settings title
        val titleText = TextView(this).apply {
            text = "Settings"
            textSize = 30f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            gravity = Gravity.CENTER_VERTICAL
            setShadowLayer(12f, 0f, 2f, Color.parseColor("#1C2F6D"))
        }
        
        headerLayout.addView(backButton)
        headerLayout.addView(titleText)
        contentLayout.addView(headerLayout)
        // Divider line
        contentLayout.addView(View(this).apply {
            setBackgroundColor(Color.parseColor("#22304A"))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2).apply { setMargins(0, 24, 0, 24) }
        })
        
        // Add spacing
        contentLayout.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                40
            )
        })
        
        // Time Format Setting
        contentLayout.addView(createTimeFormatSection())
        
        // App Information Section
        contentLayout.addView(createAppInfoSection())
        
        // Legal Section
        contentLayout.addView(createLegalSection())
        
        scrollView.addView(contentLayout)
        mainLayout.addView(scrollView)
        setContentView(mainLayout)
    }
    
    private fun createTimeFormatSection(): LinearLayout {
        val section = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 20, 0, 20)
        }
        
        // Section title
        val sectionTitle = TextView(this).apply {
            text = "Display"
            textSize = 20f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        section.addView(sectionTitle)
        
        // Time format switch
        val timeFormatLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_VERTICAL
            setPadding(20, 20, 20, 20)
        }
        
        val timeFormatLabel = TextView(this).apply {
            text = "24-Hour Format"
            textSize = 16f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        
        val timeFormatSwitch = Switch(this).apply {
            isChecked = sharedPrefs.getBoolean("is24hFormat", true)
            setOnCheckedChangeListener { _, isChecked ->
                sharedPrefs.edit().putBoolean("is24hFormat", isChecked).apply()
                // Notify main activity of change
                setResult(RESULT_OK, Intent().apply {
                    putExtra("timeFormatChanged", true)
                })
            }
        }
        
        timeFormatLayout.addView(timeFormatLabel)
        timeFormatLayout.addView(timeFormatSwitch)
        section.addView(timeFormatLayout)
        
        return section
    }
    
    private fun createAppInfoSection(): LinearLayout {
        val section = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 20, 0, 20)
        }
        
        // Section title
        val sectionTitle = TextView(this).apply {
            text = "App Information"
            textSize = 20f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        section.addView(sectionTitle)
        
        // App version
        val versionLayout = createInfoRow("Version", getAppVersion())
        section.addView(versionLayout)
        
        // Developer contact (hidden by default)
        val contactLayout = createExpandableInfoRow("Developer Contact", "adilkt16@gmail.com")
        section.addView(contactLayout)
        
        return section
    }
    
    private fun createLegalSection(): LinearLayout {
        val section = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 20, 0, 20)
        }
        
        // Section title
        val sectionTitle = TextView(this).apply {
            text = "Legal"
            textSize = 20f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        section.addView(sectionTitle)
        
        // Privacy Policy
        val privacyButton = createLinkButton("Privacy Policy") {
            openPrivacyPolicy()
        }
        section.addView(privacyButton)
        
        // License Info
        val licenseButton = createLinkButton("License Information") {
            showLicenseInfo()
        }
        section.addView(licenseButton)
        
        return section
    }
    
    private fun createInfoRow(label: String, value: String): LinearLayout {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(20, 15, 20, 15)
        }
        
        val labelText = TextView(this).apply {
            text = label
            textSize = 16f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        
        val valueText = TextView(this).apply {
            text = value
            textSize = 16f
            setTextColor(Color.parseColor("#B0BEC5"))
            gravity = Gravity.END
        }
        
        layout.addView(labelText)
        layout.addView(valueText)
        
        return layout
    }
    
    private fun createExpandableInfoRow(label: String, hiddenValue: String): LinearLayout {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(20, 15, 20, 15)
            background = ContextCompat.getDrawable(this@SettingsActivity, android.R.drawable.list_selector_background)
        }
        
        val labelText = TextView(this).apply {
            text = label
            textSize = 16f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        
        val arrowText = TextView(this).apply {
            text = "▶"
            textSize = 14f
            setTextColor(Color.parseColor("#B0BEC5"))
        }
        
        val hiddenLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(40, 10, 20, 15)
            visibility = View.GONE
        }
        
        val hiddenText = TextView(this).apply {
            text = hiddenValue
            textSize = 14f
            setTextColor(Color.parseColor("#81C784"))
            isClickable = true
            setOnClickListener {
                // Copy to clipboard
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Contact", hiddenValue)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this@SettingsActivity, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }
        
        hiddenLayout.addView(hiddenText)
        
        mainLayout.setOnClickListener {
            if (hiddenLayout.visibility == View.GONE) {
                hiddenLayout.visibility = View.VISIBLE
                arrowText.text = "▼"
            } else {
                hiddenLayout.visibility = View.GONE
                arrowText.text = "▶"
            }
        }
        
        mainLayout.addView(labelText)
        mainLayout.addView(arrowText)
        
        container.addView(mainLayout)
        container.addView(hiddenLayout)
        
        return container
    }
    
    private fun createLinkButton(buttonText: String, onClick: () -> Unit): LinearLayout {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(20, 15, 20, 15)
            background = ContextCompat.getDrawable(this@SettingsActivity, android.R.drawable.list_selector_background)
            setOnClickListener { onClick() }
        }
        
        val textView = TextView(this).apply {
            text = buttonText
            textSize = 16f
            setTextColor(Color.parseColor("#81C784"))
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        
        val arrowText = TextView(this).apply {
            text = "→"
            textSize = 16f
            setTextColor(Color.parseColor("#B0BEC5"))
        }
        
        layout.addView(textView)
        layout.addView(arrowText)
        
        return layout
    }
    
    private fun getAppVersion(): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0"
        }
    }
    
    private fun openPrivacyPolicy() {
        try {
            // Open the GitHub repository link in browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/adilkt16/modern-clock-app"))
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to dialog if browser can't be opened
            showPrivacyPolicyDialog()
        }
    }
    
    private fun showPrivacyPolicyDialog() {
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Privacy Policy")
            .setMessage("This app stores alarm settings locally on your device. We do not collect or share your personal data.\n\nFor the full privacy policy, visit:\nhttps://github.com/adilkt16/modern-clock-app")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setNeutralButton("Open Link") { _, _ ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/adilkt16/modern-clock-app"))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Could not open link", Toast.LENGTH_SHORT).show()
                }
            }
            .create()
        
        dialog.show()
    }
    
    private fun showLicenseInfo() {
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("License Information")
            .setMessage("""
                Modern Clock App
                Version 1.0
                
                This app is developed by ADIL.
                
                Uses Android SDK components under Apache License 2.0.
                
                For support, contact: adilkt16@gmail.com
            """.trimIndent())
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
        
        dialog.show()
    }
}