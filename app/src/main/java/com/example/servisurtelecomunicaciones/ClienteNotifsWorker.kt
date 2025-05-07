package com.example.servisurtelecomunicaciones

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ClienteNotifsWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            androidx.core.content.ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.POST_NOTIFICATIONS)
            != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val intent = Intent(applicationContext, IncidentActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("¿Todo va bien?")
            .setContentText("Revisa tus servicios. Puedes reportar una incidencia si hace falta.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(2001, notification)

        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "cliente_revision_semanal"

        fun scheduleWeekly(context: Context) {
            val now = Calendar.getInstance()
            val next = now.clone() as Calendar

            next.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            next.set(Calendar.HOUR_OF_DAY, 10)
            next.set(Calendar.MINUTE, 0)
            next.set(Calendar.SECOND, 0)

            if (now.after(next)) {
                next.add(Calendar.WEEK_OF_YEAR, 1)
            }

            val delay = next.timeInMillis - now.timeInMillis

            val request = androidx.work.PeriodicWorkRequestBuilder<ClienteNotifsWorker>(7, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()

            androidx.work.WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork("cliente_revision_semanal", ExistingPeriodicWorkPolicy.UPDATE, request)
        }
    }
}
