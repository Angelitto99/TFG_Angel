// src/main/java/com/example/servisurtelecomunicaciones/PendientesWorker.kt
package com.example.servisurtelecomunicaciones

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.database.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class PendientesWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    companion object {
        const val CHANNEL_ID   = "pendientes_diarios"
        private const val CHANNEL_NAME = "Recordatorio diario"
        private const val CHANNEL_DESC = "Aviso de presupuestos y facturas pendientes"
    }

    override suspend fun doWork(): Result {
        // inicializar RTDB
        val db = FirebaseDatabase
            .getInstance("https://servisurtelecomunicacion-223b0-default-rtdb.firebaseio.com")

        val presRef = db.getReference("presupuestos")
        val facRef  = db.getReference("facturas")

        // 1) contar presupuestos pendientes
        val presCount = presRef
            .orderByChild("estado").equalTo("pendiente")
            .awaitCount()

        // 2) contar facturas pendientes
        val facCount = facRef
            .orderByChild("estado").equalTo("pendiente")
            .awaitCount()

        val total = presCount + facCount
        if (total > 0) showNotification(total, presCount, facCount)
        return Result.success()
    }

    private suspend fun Query.awaitCount(): Long =
        suspendCancellableCoroutine { cont ->
            this.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    cont.resume(snap.childrenCount)
                }
                override fun onCancelled(err: DatabaseError) {
                    cont.resume(0L)
                }
            })
        }

    private fun showNotification(total: Long, pres: Long, fac: Long) {
        val nm = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        // Registrar canal en Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = CHANNEL_DESC }
            nm.createNotificationChannel(chan)
        }

        // Texto según lo que haya
        val detail = when {
            pres > 0 && fac > 0 -> "Presupuestos: $pres • Facturas: $fac"
            pres > 0            -> "Presupuestos pendientes: $pres"
            else                -> "Facturas pendientes: $fac"
        }

        val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notificacion) // <– Coloca tu icono de campana aquí
            .setContentTitle("Tienes $total pendientes hoy")
            .setContentText(detail)
            .setAutoCancel(true)
            .build()

        nm.notify(1001, notif)
    }
}
