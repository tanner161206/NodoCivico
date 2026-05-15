package com.nodocivico.app.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.app.NotificationCompat
import com.nodocivico.app.NodoCivicoApp
import com.nodocivico.app.R
import com.nodocivico.app.notifications.NotificationScheduler
import com.nodocivico.app.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConnectivityReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_CONNECTIVITY_CHANGED = "com.nodocivico.CONNECTIVITY_CHANGED"
        var onConnectivityChanged: ((Boolean) -> Unit)? = null
    }
    override fun onReceive(context: Context, intent: Intent) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }
        val connected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        onConnectivityChanged?.invoke(connected)
        context.sendBroadcast(Intent(ACTION_CONNECTIVITY_CHANGED).putExtra("is_connected", connected))
    }
}

class ReminderReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID      = "nodo_civico_reminders"
        const val CHANNEL_NAME    = "Recordatorios Nodo Cívico"
        const val EXTRA_TITLE     = "reminder_title"
        const val EXTRA_MESSAGE   = "reminder_message"
        const val EXTRA_REPORT_ID = "report_id"
        const val EXTRA_NOTIF_ID  = "notification_id"
    }
    override fun onReceive(context: Context, intent: Intent) {
        val title    = intent.getStringExtra(EXTRA_TITLE)   ?: "Recordatorio"
        val message  = intent.getStringExtra(EXTRA_MESSAGE) ?: "Seguimiento pendiente"
        val reportId = intent.getLongExtra(EXTRA_REPORT_ID, -1L)
        val notifId  = intent.getIntExtra(EXTRA_NOTIF_ID, reportId.toInt())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
        }
        val tap = PendingIntent.getActivity(
            context, notifId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to_report", reportId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title).setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(tap).setAutoCancel(true).build()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(notifId, notif)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as NodoCivicoApp
                val reminders = app.reminderRepository.getUpcomingReminders()
                NotificationScheduler.rescheduleAll(context, reminders)
            } finally {
                pending.finish()
            }
        }
    }
}
