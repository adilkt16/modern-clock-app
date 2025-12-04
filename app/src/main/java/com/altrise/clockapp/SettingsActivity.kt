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
        // Main container with matching improved gradient - better readability
        val mainLayout = FrameLayout(this).apply {
            background = android.graphics.drawable.GradientDrawable().apply {
                orientation = android.graphics.drawable.GradientDrawable.Orientation.TL_BR
                colors = intArrayOf(
                    Color.parseColor("#C8B6E2"), // Deeper lavender
                    Color.parseColor("#A0D8F1"), // Rich sky blue
                    Color.parseColor("#B0E8D8")  // Deeper mint
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
        
        // Improved back button with better contrast
        val backButton = TextView(this).apply {
            text = "← Back"
            textSize = 16f
            setTextColor(Color.parseColor("#1A1F3A")) // Dark for strong contrast
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            letterSpacing = 0.03f
            setPadding(24, 12, 24, 12)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 24, 0) }
            // Stronger glassmorphic background
            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 24f * resources.displayMetrics.density
                setColor(Color.parseColor("#70FFFFFF"))
                setStroke((2f * resources.displayMetrics.density).toInt(), Color.parseColor("#90FFFFFF"))
            }
            elevation = 2f * resources.displayMetrics.density
            isClickable = true
            isFocusable = true
            // Smooth press animation
            setOnTouchListener { v, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        v.animate().scaleX(0.92f).scaleY(0.92f).setDuration(100).start()
                    }
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                    }
                }
                false
            }
            setOnClickListener { finish() }
            contentDescription = "Go back"
        }
        
        // Settings title with strong contrast
        val titleText = TextView(this).apply {
            text = "Settings"
            textSize = 32f
            setTextColor(Color.parseColor("#1A1F3A")) // Dark for strong contrast
            typeface = Typeface.create("sans-serif-light", Typeface.BOLD)
            letterSpacing = 0.05f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            gravity = Gravity.CENTER_VERTICAL
            setShadowLayer(2f, 0f, 1f, Color.parseColor("#30FFFFFF"))
        }
        
        headerLayout.addView(backButton)
        headerLayout.addView(titleText)
        contentLayout.addView(headerLayout)
        
        // Divider with better visibility
        contentLayout.addView(View(this).apply {
            background = android.graphics.drawable.GradientDrawable().apply {
                orientation = android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT
                colors = intArrayOf(
                    Color.TRANSPARENT,
                    Color.parseColor("#50000000"),
                    Color.TRANSPARENT
                )
            }
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2).apply { setMargins(0, 28, 0, 32) }
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
        
        // Buy Me a Coffee Section - Support Development
        contentLayout.addView(createBuyMeCoffeeSection())
        
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
            setPadding(0, 12, 0, 20)
        }
        
        // Section title with better contrast
        val sectionTitle = TextView(this).apply {
            text = "Display"
            textSize = 18f
            setTextColor(Color.parseColor("#1A1F3A")) // Dark for readability
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            letterSpacing = 0.04f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(4, 0, 0, 16)
        }
        section.addView(sectionTitle)
        
        // Card container with improved contrast
        val cardContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor("#75FFFFFF")) // More opaque glass effect
                cornerRadius = 32f
                setStroke(2, Color.parseColor("#AAFFFFFF")) // Thicker, visible border
            }
            elevation = 6f // Increased elevation for depth
            setPadding(28, 24, 28, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24)
            }
        }

        // Version label with strong contrast
        val versionLabel = TextView(this).apply {
            text = "Version"
            textSize = 16f
            setTextColor(Color.parseColor("#1A1F3A")) // Dark for readability
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        val timeFormatLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_VERTICAL
        }
        
        val timeFormatLabel = TextView(this).apply {
            text = "24-Hour Format"
            textSize = 16f
            setTextColor(Color.parseColor("#1A1F3A")) // Dark for readability
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
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
        cardContainer.addView(timeFormatLayout)
        section.addView(cardContainer)
        
        return section
    }
    
    private fun createAppInfoSection(): LinearLayout {
        val section = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 12, 0, 20)
        }
        
        // Section title with better contrast
        val sectionTitle = TextView(this).apply {
            text = "App Information"
            textSize = 18f
            setTextColor(Color.parseColor("#1A1F3A")) // Dark for readability
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            letterSpacing = 0.04f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(4, 0, 0, 16)
        }
        section.addView(sectionTitle)
        
        // App version
        val versionLayout = createInfoRow("Version", getAppVersion())
        section.addView(versionLayout)
        
        return section
    }
    
    private fun createBuyMeCoffeeSection(): LinearLayout {
        val section = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 12, 0, 20)
        }
        
        // Section title
        val sectionTitle = TextView(this).apply {
            text = "Support Development"
            textSize = 18f
            setTextColor(Color.parseColor("#1A1F3A"))
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            letterSpacing = 0.04f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(4, 0, 0, 16)
        }
        section.addView(sectionTitle)
        
        // Attractive Buy Me a Coffee card
        val coffeeCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 32f
                // Beautiful gradient from warm yellow to orange
                colors = intArrayOf(
                    Color.parseColor("#FFF4E6"),
                    Color.parseColor("#FFE8CC")
                )
                setStroke(2, Color.parseColor("#FFD580"))
            }
            elevation = 6f
            setPadding(32, 28, 32, 28)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            isClickable = true
            isFocusable = true
            setOnClickListener {
                openBuyMeCoffee()
            }
        }
        
        // Coffee emoji and title
        val titleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        val coffeeEmoji = TextView(this).apply {
            text = "☕"
            textSize = 42f
            setPadding(0, 0, 16, 0)
        }
        
        val titleText = TextView(this).apply {
            text = "Buy Me a Coffee"
            textSize = 22f
            setTextColor(Color.parseColor("#1A1F3A"))
            typeface = Typeface.create("sans-serif-bold", Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        
        titleRow.addView(coffeeEmoji)
        titleRow.addView(titleText)
        
        // Compelling message
        val messageText = TextView(this).apply {
            text = "Love this app? ❤️\n\nYour support keeps me motivated to create more amazing features and maintain this app for free. Every coffee counts!"
            textSize = 15f
            setTextColor(Color.parseColor("#3A4560"))
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            lineHeight = (22 * resources.displayMetrics.density).toInt()
            setPadding(0, 16, 0, 20)
        }
        
        // Call-to-action button
        val ctaButton = Button(this).apply {
            text = "☕ Support with Coffee"
            textSize = 16f
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            letterSpacing = 0.04f
            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 24f * resources.displayMetrics.density
                colors = intArrayOf(
                    Color.parseColor("#FF9800"),
                    Color.parseColor("#FF6F00")
                )
            }
            elevation = 4f
            setPadding(32, 18, 32, 18)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                openBuyMeCoffee()
            }
        }
        
        // Add touch animation
        ctaButton.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            false
        }
        
        coffeeCard.addView(titleRow)
        coffeeCard.addView(messageText)
        coffeeCard.addView(ctaButton)
        
        section.addView(coffeeCard)
        
        // Small thank you note
        val thankYouText = TextView(this).apply {
            text = "Thank you for using ClockApp! 🙏"
            textSize = 13f
            setTextColor(Color.parseColor("#7A8099"))
            typeface = Typeface.create("sans-serif-light", Typeface.ITALIC)
            gravity = Gravity.CENTER
            setPadding(20, 8, 20, 0)
        }
        section.addView(thankYouText)
        
        return section
    }
    
    private fun openBuyMeCoffee() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/a_k_t_16"))
            startActivity(intent)
            Toast.makeText(this, "☕ Thank you for your support!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open browser", Toast.LENGTH_SHORT).show()
        }
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
            setTextColor(Color.parseColor("#5B8FDB")) // Richer blue for better readability
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
            setTextColor(Color.parseColor("#5B8FDB")) // Richer blue for better readability
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        
        val arrowText = TextView(this).apply {
            text = "→"
            textSize = 16f
            setTextColor(Color.parseColor("#3A4560")) // Darker gray
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