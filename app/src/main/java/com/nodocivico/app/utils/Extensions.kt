package com.nodocivico.app.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

// ---------------------------------------------------------------------------
// Snackbar helpers
// ---------------------------------------------------------------------------

fun View.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).show()
}

fun View.showSnackbarError(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG)
        .setBackgroundTint(resources.getColor(android.R.color.holo_red_dark, null))
        .show()
}

fun View.showSnackbarSuccess(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
        .setBackgroundTint(resources.getColor(android.R.color.holo_green_dark, null))
        .show()
}

// ---------------------------------------------------------------------------
// View visibility helpers
// ---------------------------------------------------------------------------

fun View.visible() { visibility = View.VISIBLE }
fun View.gone()    { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

// ---------------------------------------------------------------------------
// Date formatting
// ---------------------------------------------------------------------------

fun Long.toFormattedDate(pattern: String = "dd/MM/yyyy HH:mm"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    return when {
        diff < 60_000       -> "Ahora"
        diff < 3_600_000    -> "Hace ${diff / 60_000} min"
        diff < 86_400_000   -> "Hace ${diff / 3_600_000} h"
        diff < 172_800_000  -> "Ayer"
        else                -> toFormattedDate("dd/MM/yyyy")
    }
}

// ---------------------------------------------------------------------------
// Validation helpers
// ---------------------------------------------------------------------------

fun String.isValidEmail(): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isNotBlankOrEmpty(): Boolean = this.isNotBlank() && this.isNotEmpty()
