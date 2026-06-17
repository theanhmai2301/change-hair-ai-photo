package com.exo.hairstyleai.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/** Outbound links & actions used by Settings and the report flow. */
object AppLinks {

    const val SUPPORT_EMAIL = "exostudio.feedback@gmail.com"

    // TODO: replace with your real hosted privacy policy URL.
    const val PRIVACY_POLICY_URL = "https://exostudio24.github.io/hairstyle-ai/privacy"

    fun openUrl(context: Context, url: String) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    fun shareApp(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }

    /** Open Play Store on the app's listing (falls back to the web URL). */
    fun rateApp(context: Context) {
        val pkg = context.packageName
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")))
        } catch (e: ActivityNotFoundException) {
            openUrl(context, "https://play.google.com/store/apps/details?id=$pkg")
        }
    }

    /** Compose an email to support, optionally pre-filled. */
    fun email(context: Context, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        runCatching {
            context.startActivity(Intent.createChooser(intent, null))
        }
    }
}
