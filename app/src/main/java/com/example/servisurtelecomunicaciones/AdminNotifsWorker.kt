package com.example.servisurtelecomunicaciones

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AdminNotifsWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        // Validar día de la semana: solo de lunes (2) a viernes (6)
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        if (today == Calendar.SATURDAY || today == Calendar.SUNDAY) {
            return Result.success()
        }

        // Verificar permiso
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        // Crear canal si es necesario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
            }
            val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }

        // Crear notificación
        val intent = Intent(applicationContext, AdminHomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("Revisión diaria de pendientes")
            .setContentText("Revisa presupuestos y facturas pendientes.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(3001, notification)

        return Result.success()
    }

    companion object {
        private const val CHANNEL_ID = "admin_diarios"
        private const val CHANNEL_NAME = "Aviso diario admin"
        private const val CHANNEL_DESC = "Notificación diaria de pendientes (lunes a viernes)"

        fun scheduleDaily(context: Context) {
            val now = Calendar.getInstance()
            val next = now.clone() as Calendar
            next.set(Calendar.HOUR_OF_DAY, 10)
            next.set(Calendar.MINUTE, 0)
            next.set(Calendar.SECOND, 0)

            if (now.after(next)) {
                next.add(Calendar.DAY_OF_YEAR, 1)
            }

            val delay = next.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<AdminNotifsWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag("admin_diarios")
                .build()

            androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "admin_diarios",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}
