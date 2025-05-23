package com.example.servisurtelecomunicaciones

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.content.pm.PackageManager
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.util.*

class AlarmManagerAdmin : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
            Log.d("AlarmManagerAdmin", "Fin de semana. No se notifica.")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Aviso diario admin",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificación diaria de pendientes (lunes a viernes)"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val intentActivity = Intent(context, AdminHomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intentActivity,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )



        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logonoti)
            .setContentTitle("Pendientes de gestión")
            .setContentText("¿Tienes facturas o presupuestos pendientes por revisar?")
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(3001, notification)
        }

        programarAlarmaDiaria(context)
    }

    companion object {
        const val CHANNEL_ID = "admin_diarios"

        fun programarAlarmaDiaria(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, AlarmManagerAdmin::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 10)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()
            ) {
                Log.w("AlarmManagerAdmin", "No se puede programar alarma exacta: permiso denegado por el usuario")
                return
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

            Log.d("AlarmManagerAdmin", "Alarma diaria programada para las 10:00")
        }

        fun cancelarAlarma(context: Context) {
            val intent = Intent(context, AlarmManagerAdmin::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)

            Log.d("AlarmManagerAdmin", "Alarma cancelada")
        }
    }
}
