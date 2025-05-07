package com.example.servisurtelecomunicaciones

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.google.android.material.textfield.TextInputEditText
import de.hdodenhof.circleimageview.CircleImageView
import java.util.concurrent.TimeUnit

class EditarPerfilActivity : AppCompatActivity() {

    companion object {
        private const val PREFS = "prefs"
        private const val KEY_NAME = "user_name"
        private const val KEY_AVATAR = "user_avatar"
        private const val KEY_NOTIFS = "user_notifs"
        private const val RC_PICK_PHOTO = 1001
        private const val RC_NOTIF_PERM = 2001
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

        ivAvatar = findViewById(R.id.ivAvatar)
        etNombre = findViewById(R.id.etNombre)
        etDireccion = findViewById(R.id.etDireccion)
        etTelefono = findViewById(R.id.etTelefono)
        swNotifs = findViewById(R.id.swNotifs)
        btnGuardar = findViewById(R.id.btnGuardarPerfil)

        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        etNombre.setText(prefs.getString(KEY_NAME, ""))
        swNotifs.isChecked = prefs.getBoolean(KEY_NOTIFS, false)

        ivAvatar.setOnClickListener { pickImage() }

        btnGuardar.setOnClickListener {
            prefs.edit()
                .putString(KEY_NAME, etNombre.text.toString())
                .putBoolean(KEY_NOTIFS, swNotifs.isChecked)
                .apply()

            val isAdmin = prefs.getBoolean("is_admin", false)
            if (swNotifs.isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), RC_NOTIF_PERM)
                } else {
                    if (!isAdmin) ClienteNotifsWorker.scheduleWeekly(this)
                    // Aquí podrías poner el Worker del admin si deseas
                }
            } else {
                WorkManager.getInstance(this).cancelAllWorkByTag("cliente_revision_semanal")
            }

            Toast.makeText(this, "Perfil guardado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, RC_PICK_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_PICK_PHOTO && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                ivAvatar.setImageURI(uri)
                getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_AVATAR, uri.toString())
                    .apply()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_NOTIF_PERM && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ClienteNotifsWorker.scheduleWeekly(this)
        }
    }
}
