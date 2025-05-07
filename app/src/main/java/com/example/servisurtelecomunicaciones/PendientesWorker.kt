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
import androidx.work.WorkerParameters
import com.google.firebase.database.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class PendientesWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    companion object {
        const val CHANNEL_ID = "pendientes_diarios"
        private const val CHANNEL_NAME = "Recordatorio diario"
        private const val CHANNEL_DESC = "Aviso de presupuestos y facturas pendientes"
    }

    override suspend fun doWork(): Result {
        val db = FirebaseDatabase.getInstance("https://servisurtelecomunicacion-223b0-default-rtdb.firebaseio.com")
        val presRef = db.getReference("presupuestos")
        val facRef = db.getReference("facturas")

        val presCount = presRef.orderByChild("estado").equalTo("pendiente").awaitCount()
        val facCount = facRef.orderByChild("estado").equalTo("pendiente").awaitCount()

        val total = presCount + facCount
        if (total > 0) showNotification(total, presCount, facCount)
        return Result.success()
    }

    private suspend fun Query.awaitCount(): Long =
        suspendCancellableCoroutine { cont ->
            this.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    cont.resume(snap.childrenCount)
                }
                override fun onCancelled(error: DatabaseError) {
                    cont.resume(0L)
                }
            })
        }

    private fun showNotification(total: Long, pres: Long, fac: Long) {
        val prefs = applicationContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val isAdmin = prefs.getBoolean("is_admin", false)

        val intent = Intent(applicationContext, if (isAdmin) AdminHomeActivity::class.java else HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = CHANNEL_DESC
            }
            val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }

        val detail = when {
            pres > 0 && fac > 0 -> "Presupuestos: $pres • Facturas: $fac"
            pres > 0 -> "Presupuestos pendientes: $pres"
            else -> "Facturas pendientes: $fac"
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("Tienes $total pendientes hoy")
            .setContentText(detail)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(1001, notification)
    }
}
