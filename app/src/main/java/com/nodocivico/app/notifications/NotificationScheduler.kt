package com.nodocivico.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.nodocivico.app.domain.model.Reminder
import com.nodocivico.app.receivers.ReminderReceiver

object NotificationScheduler {

    /** Programa una alarma exacta para mostrar la notificación del recordatorio */
    fun schedule(context: Context, reminder: Reminder) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = buildPendingIntent(context, reminder)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.reminderDate, pi)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, reminder.reminderDate, pi)
        }
    }

    /** Cancela una alarma programada */
    fun cancel(context: Context, reminderId: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            context, reminderId.toInt(),
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
    }

    /** Reprograma todos los recordatorios futuros (llamado desde BootReceiver) */
    suspend fun rescheduleAll(context: Context, reminders: List<Reminder>) {
        val now = System.currentTimeMillis()
        reminders.filter { it.reminderDate > now }.forEach { schedule(context, it) }
    }

    private fun buildPendingIntent(context: Context, reminder: Reminder): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_TITLE,     "Recordatorio Nodo Cívico")
            putExtra(ReminderReceiver.EXTRA_MESSAGE,   reminder.message)
            putExtra(ReminderReceiver.EXTRA_REPORT_ID, reminder.reportId)
            putExtra(ReminderReceiver.EXTRA_NOTIF_ID,  reminder.id.toInt())
        }
        return PendingIntent.getBroadcast(
            context, reminder.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
