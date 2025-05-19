package com.example.servisurtelecomunicaciones

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class HomeActivity : AppCompatActivity() {

    companion object {
        private const val PREFS = "prefs"
        private const val KEY_NAME = "user_name"
        private const val KEY_AVATAR = "user_avatar"
        private const val KEY_NOTIFS = "user_notifs"
        private const val ADMIN_NAME = "admin_name"
        private const val ADMIN_AVATAR = "admin_avatar"
    }

    private lateinit var ivAvatar: CircleImageView
    private lateinit var tvGreeting: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        ivAvatar = findViewById(R.id.ivUserAvatar)
        tvGreeting = findViewById(R.id.tvHolaUsuario)

        ivAvatar.setOnClickListener {
            val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val isAdmin = prefs.getBoolean("is_admin", false)
            if (!prefs.getBoolean("is_guest", false)) {
                val intent = if (isAdmin) {
                    Intent(this, EditarPerfilAdminActivity::class.java)
                } else {
                    Intent(this, EditarPerfilClienteActivity::class.java)
                }
                startActivity(intent)
            } else {
                toastConLogo("Debes registrarte para acceder al perfil")
            }
        }

        findViewById<CardView>(R.id.Antenas).setOnClickListener {
            startActivity(Intent(this, AntenaActivity::class.java))
        }
        Glide.with(this).load(R.drawable.ic_satellite_new).into(findViewById(R.id.iconSatelliteTV))

        findViewById<CardView>(R.id.cardVideoporteros).setOnClickListener {
            startActivity(Intent(this, VideoporteroActivity::class.java))
        }
        Glide.with(this).load(R.drawable.ic_videoportero_new).into(findViewById(R.id.iconVideoporteros))

        findViewById<CardView>(R.id.cardElectricidad).setOnClickListener {
            startActivity(Intent(this, ElectricidadActivity::class.java))
        }
        Glide.with(this).load(R.drawable.ic_electricidad_new).into(findViewById(R.id.iconElectricidad))

        findViewById<CardView>(R.id.cardCCTV).setOnClickListener {
            startActivity(Intent(this, CCTVActivity::class.java))
        }
        findViewById<ImageView>(R.id.iconCCTV).setImageResource(R.drawable.ic_cctv)

        findViewById<CardView>(R.id.cardCerrajeria).setOnClickListener {
            startActivity(Intent(this, CerrajeriaActivity::class.java))
        }
        findViewById<ImageView>(R.id.iconCerrajeria).setImageResource(R.drawable.ic_cerrajeria)

        findViewById<CardView>(R.id.cardMantenimiento).setOnClickListener {
            startActivity(Intent(this, MantenimientoActivity::class.java))
        }
        findViewById<ImageView>(R.id.iconMantenimiento).setImageResource(R.drawable.ic_mantenimiento)

        findViewById<Button>(R.id.buttonAboutUs).setOnClickListener {
            startActivity(Intent(this, AboutUsActivity::class.java))
        }

        findViewById<Button>(R.id.websiteButton).setOnClickListener {
            val url = "https://www.servisurtelecomunicaciones.com/"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        findViewById<Button>(R.id.buttonIncident).setOnClickListener {
            val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            if (!prefs.getBoolean("is_guest", false)) {
                startActivity(Intent(this, IncidentActivity::class.java))
            } else {
                toastConLogo("Debes registrarte para usar esta función")
            }
        }

        findViewById<Button>(R.id.btnCerrarSesion).setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            prefs.edit()
                .remove("is_guest")
                .remove("is_admin")
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
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val isGuest = prefs.getBoolean("is_guest", false)
        val isAdmin = prefs.getBoolean("is_admin", false)

        val nombre = when {
            isGuest -> "Invitado"
            isAdmin -> prefs.getString(ADMIN_NAME, "Administrador") ?: "Administrador"
            else -> prefs.getString(KEY_NAME, "Usuario") ?: "Usuario"
        }
        tvGreeting.text = "Hola, $nombre"

        val avatarPath = when {
            isGuest -> null
            isAdmin -> prefs.getString(ADMIN_AVATAR, null)
            else -> prefs.getString(KEY_AVATAR, null)
        }

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

        if (!isGuest && prefs.getBoolean(KEY_NOTIFS, false)) {
            if (isAdmin) AlarmManagerAdmin.programarAlarmaDiaria(this)
            else AlarmManagerCliente.programarAlarmaSemanal(this)
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
