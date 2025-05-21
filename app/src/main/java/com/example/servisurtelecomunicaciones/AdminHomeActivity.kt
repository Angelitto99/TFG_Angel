package com.example.servisurtelecomunicaciones

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var ivAvatar: CircleImageView
    private lateinit var tvGreeting: TextView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        ivAvatar   = findViewById(R.id.ivAdminAvatar)
        tvGreeting = findViewById(R.id.tvHolaAdmin)
        btnLogout  = findViewById(R.id.btnCerrarSesionAdmin)

        ivAvatar.setOnClickListener {
            startActivity(Intent(this, EditarPerfilAdminActivity::class.java))
        }

        findViewById<CardView>(R.id.cardVerIncidencias).setOnClickListener {
            startActivity(Intent(this, IncidenciasListActivity::class.java))
        }

        findViewById<CardView>(R.id.cardReportar).setOnClickListener {
            startActivity(Intent(this, IncidentActivity::class.java))
        }

        findViewById<CardView>(R.id.cardFacturas).setOnClickListener {
            startActivity(Intent(this, FacturasActivity::class.java))
        }

        findViewById<CardView>(R.id.cardPresupuestos).setOnClickListener {
            startActivity(Intent(this, PresupuestosActivity::class.java))
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
            toastConLogo("Sesión cerrada correctamente")
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: prefs.getString("last_uid", "")!!.also {
                // si el usuario vino por "olvidé contraseña", usamos last_uid
            }

        val ref = FirebaseDatabase.getInstance()
            .getReference("perfiles/admins/$uid")

        ref.get().addOnSuccessListener { snapshot ->
            val nombre     = snapshot.child("nombre").getValue(String::class.java) ?: "Administrador"
            val avatarPath = snapshot.child("avatarPath").getValue(String::class.java)
            val notifs     = snapshot.child("notificaciones").getValue(Boolean::class.java) ?: false

            tvGreeting.text = "Hola, $nombre"

            if (avatarPath != null && File(avatarPath).exists()) {
                ivAvatar.setImageBitmap(getCorrectlyOrientedBitmap(avatarPath))
            } else {
                ivAvatar.setImageResource(R.drawable.ic_perfil)
            }

            if (notifs) AlarmManagerAdmin.programarAlarmaDiaria(this)
            else        AlarmManagerAdmin.cancelarAlarma(this)
        }
    }

    private fun getCorrectlyOrientedBitmap(imagePath: String) =
        BitmapFactory.decodeFile(imagePath).let { bitmap ->
            val exif = androidx.exifinterface.media.ExifInterface(imagePath)
            val orient = exif.getAttributeInt(
                androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
            )
            Matrix().apply {
                when (orient) {
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90  -> postRotate(90f)
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
                }
            }.let { matrix ->
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
        }

    private fun toastConLogo(msg: String) {
        val layout = layoutInflater.inflate(
            R.layout.toast_custom_logo,
            findViewById(android.R.id.content),
            false
        )
        layout.findViewById<TextView>(R.id.toastText).text = msg
        Toast(applicationContext).apply {
            duration = Toast.LENGTH_SHORT
            view     = layout
            setGravity(Gravity.CENTER, 0, 250)
            show()
        }
    }
}
