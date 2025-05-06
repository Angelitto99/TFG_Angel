// src/main/java/com/example/servisurtelecomunicaciones/EditarPerfilActivity.kt
package com.example.servisurtelecomunicaciones

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import de.hdodenhof.circleimageview.CircleImageView
import com.google.android.material.textfield.TextInputEditText
import java.util.concurrent.TimeUnit

class EditarPerfilActivity : AppCompatActivity() {

    companion object {
        private const val PREFS         = "prefs"
        private const val KEY_NOTIFS    = "user_notifs"
        private const val RC_PICK_PHOTO = 1001
    }

    private lateinit var ivAvatar: CircleImageView
    private lateinit var etNombre: TextInputEditText
    private lateinit var etDireccion: TextInputEditText
    private lateinit var etTelefono: TextInputEditText
    private lateinit var swNotifs: Switch
    private lateinit var btnGuardar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)

        ivAvatar    = findViewById(R.id.ivAvatar)
        etNombre    = findViewById(R.id.etNombre)
        etDireccion = findViewById(R.id.etDireccion)
        etTelefono  = findViewById(R.id.etTelefono)
        swNotifs    = findViewById(R.id.swNotifs)
        btnGuardar  = findViewById(R.id.btnGuardarPerfil)

        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        swNotifs.isChecked = prefs.getBoolean(KEY_NOTIFS, false)

        ivAvatar.setOnClickListener { pickImage() }
        btnGuardar.setOnClickListener {
            prefs.edit().putBoolean(KEY_NOTIFS, swNotifs.isChecked).apply()
            if (swNotifs.isChecked) scheduleDailyWorker()
            else WorkManager.getInstance(this).cancelUniqueWork("pendientes_daily")
            Toast.makeText(this, "Perfil guardado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun pickImage() {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        ).setType("image/*")
        startActivityForResult(intent, RC_PICK_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_PICK_PHOTO && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                ivAvatar.setImageURI(uri)
                getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit()
                    .putString("user_avatar", uri.toString())
                    .apply()
            }
        }
    }

    private fun scheduleDailyWorker() {
        val delay = calculateNext10AMDelay()
        val req = PeriodicWorkRequestBuilder<PendientesWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "pendientes_daily",
                ExistingPeriodicWorkPolicy.UPDATE,
                req
            )
    }

    /** Calcula el retraso desde ahora hasta las 10:00 de hoy o mañana */
    private fun calculateNext10AMDelay(): Long {
        val now = java.util.Calendar.getInstance()
        val target = now.clone() as java.util.Calendar
        target.set(java.util.Calendar.HOUR_OF_DAY, 10)
        target.set(java.util.Calendar.MINUTE, 0)
        target.set(java.util.Calendar.SECOND, 0)
        if (target.before(now)) {
            target.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        // ¡Fíjate en el guión normal en vez de cualquier otro dash!
        return target.timeInMillis - now.timeInMillis
    }

}
