package com.example.servisurtelecomunicaciones

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class AdminHomeActivity : AppCompatActivity() {

    companion object {
        private const val PREFS = "prefs"
        private const val KEY_NAME = "admin_name"
        private const val KEY_AVATAR = "admin_avatar"
        private const val KEY_NOTIFS = "user_notifs"
    }

    private lateinit var ivAvatar: CircleImageView
    private lateinit var tvGreeting: TextView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        ivAvatar = findViewById(R.id.ivAdminAvatar)
        tvGreeting = findViewById(R.id.tvHolaAdmin)
        btnLogout = findViewById(R.id.btnCerrarSesionAdmin)

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

            val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            prefs.edit()
                .remove("is_guest")
                .remove("is_admin")
                .apply()

            toastConLogo("Sesión cerrada correctamente")

            Intent(this, LoginActivity::class.java).also { intent ->
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val nombre = prefs.getString(KEY_NAME, "Admin") ?: "Admin"
        tvGreeting.text = "Hola, $nombre"

        val avatarPath = prefs.getString(KEY_AVATAR, null)
        if (avatarPath != null) {
            val file = File(avatarPath)
            if (file.exists()) {
                ivAvatar.setImageBitmap(getCorrectlyOrientedBitmap(file.absolutePath))
            } else {
                ivAvatar.setImageResource(R.drawable.ic_perfil)
            }
        } else {
            ivAvatar.setImageResource(R.drawable.ic_perfil)
        }

        if (prefs.getBoolean(KEY_NOTIFS, false)) {
            AlarmManagerAdmin.programarAlarmaDiaria(this)
        }
    }

    private fun getCorrectlyOrientedBitmap(imagePath: String): Bitmap {
        val bitmap = BitmapFactory.decodeFile(imagePath)
        val exif = androidx.exifinterface.media.ExifInterface(imagePath)
        val orientation = exif.getAttributeInt(
            androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
            androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
        )

        val matrix = Matrix()
        when (orientation) {
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun toastConLogo(msg: String) {
        val layout = layoutInflater.inflate(R.layout.toast_custom_logo, findViewById(android.R.id.content), false)
        layout.findViewById<TextView>(R.id.toastText).text = msg

        Toast(applicationContext).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(Gravity.CENTER, 0, 250)
            show()
        }
    }
}
