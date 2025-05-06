// Notificacion.kt
package com.example.servisurtelecomunicaciones

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class Notificacion : Application() {
    companion object {
        const val CHANNEL_ID = "pendientes_channel"
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                CHANNEL_ID,
                "Pendientes",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorios de presupuestos pendientes"
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(chan)
        }
    }
}
