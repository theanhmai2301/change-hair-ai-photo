package com.exo.hairstyleai.firstopen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.exo.hairstyleai.MainActivity
import com.exo.hairstyleai.databinding.ActivityPermissionBinding

/** Final first-open step — opt into notifications, then enter the app. */
class PermissionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPermissionBinding
    private var requestedOnce = false

    private val requestNotif = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { reflectGranted() }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.notifSwitch.isChecked = isNotificationPermissionGranted(this)
        binding.notifRow.setOnClickListener { binding.notifSwitch.performClick() }
        binding.notifSwitch.setOnClickListener { onSwitchClicked() }
        binding.continueButton.setOnClickListener {
            MainActivity.start(this)
            finish()
        }
        // TODO: ADS — loadNativeAd(binding.frAds)
    }

    override fun onResume() {
        super.onResume()
        reflectGranted()
    }

    private fun onSwitchClicked() {
        if (!binding.notifSwitch.isChecked || isNotificationPermissionGranted(this)) {
            reflectGranted()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permanentlyDenied = requestedOnce &&
                !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
            if (permanentlyDenied) {
                openAppNotificationSettings()
            } else {
                requestedOnce = true
                requestNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun reflectGranted() {
        binding.notifSwitch.isChecked = isNotificationPermissionGranted(this)
    }

    private fun openAppNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        runCatching { startActivity(intent) }
    }

    companion object {
        fun isNotificationPermissionGranted(context: Context): Boolean =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED

        fun start(context: Context) {
            context.startActivity(Intent(context, PermissionActivity::class.java))
        }
    }
}
