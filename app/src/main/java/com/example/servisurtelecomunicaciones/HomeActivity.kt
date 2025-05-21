package com.example.servisurtelecomunicaciones

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class HomeActivity : AppCompatActivity() {

    companion object {
        private const val PREFS = "prefs"
    }

    private lateinit var ivAvatar: CircleImageView
    private lateinit var tvGreeting: TextView
    private val prefs by lazy { getSharedPreferences(PREFS, Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        ivAvatar   = findViewById(R.id.ivUserAvatar)
        tvGreeting = findViewById(R.id.tvHolaUsuario)

        val isGuest = prefs.getBoolean("is_guest", false)
        val isAdmin = prefs.getBoolean("is_admin", false)

        ivAvatar.setOnClickListener {
            if (isGuest) {
                toastConLogo("Debes registrarte para acceder al perfil")
            } else {
                val next = if (isAdmin)
                    EditarPerfilAdminActivity::class.java
                else
                    EditarPerfilClienteActivity::class.java
                startActivity(Intent(this, next))
            }
        }

        // Carga de servicios (idéntico al original)
        findViewById<CardView>(R.id.Antenas).setOnClickListener {
            startActivity(Intent(this, AntenaActivity::class.java))
        }
        Glide.with(this)
            .load(R.drawable.ic_satellite_new)
            .into(findViewById<ImageView>(R.id.iconSatelliteTV))

        findViewById<CardView>(R.id.cardVideoporteros).setOnClickListener {
            startActivity(Intent(this, VideoporteroActivity::class.java))
        }
        Glide.with(this)
            .load(R.drawable.ic_videoportero_new)
            .into(findViewById<ImageView>(R.id.iconVideoporteros))

        findViewById<CardView>(R.id.cardElectricidad).setOnClickListener {
            startActivity(Intent(this, ElectricidadActivity::class.java))
        }
        Glide.with(this)
            .load(R.drawable.ic_electricidad_new)
            .into(findViewById<ImageView>(R.id.iconElectricidad))

        findViewById<CardView>(R.id.cardCCTV).setOnClickListener {
            startActivity(Intent(this, CCTVActivity::class.java))
        }
        findViewById<ImageView>(R.id.iconCCTV)
            .setImageResource(R.drawable.ic_cctv)

        findViewById<CardView>(R.id.cardCerrajeria).setOnClickListener {
            startActivity(Intent(this, CerrajeriaActivity::class.java))
        }
        findViewById<ImageView>(R.id.iconCerrajeria)
            .setImageResource(R.drawable.ic_cerrajeria)

        findViewById<CardView>(R.id.cardMantenimiento).setOnClickListener {
            startActivity(Intent(this, MantenimientoActivity::class.java))
        }
        findViewById<ImageView>(R.id.iconMantenimiento)
            .setImageResource(R.drawable.ic_mantenimiento)

        findViewById<Button>(R.id.buttonAboutUs).setOnClickListener {
            startActivity(Intent(this, AboutUsActivity::class.java))
        }
        findViewById<Button>(R.id.websiteButton).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.servisurtelecomunicaciones.com/")))
        }

        findViewById<Button>(R.id.buttonIncident).setOnClickListener {
            if (isGuest) toastConLogo("Debes registrarte para usar esta función")
            else startActivity(Intent(this, IncidentActivity::class.java))
        }

        findViewById<Button>(R.id.btnCerrarSesion).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            prefs.edit()
                .remove("is_admin")
                .remove("is_guest")
                .remove("last_uid")
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
        val isGuest = prefs.getBoolean("is_guest", false)
        val isAdmin = prefs.getBoolean("is_admin", false)
        // Fallback UID: FirebaseAuth o last_uid
        val uid     = FirebaseAuth.getInstance().currentUser?.uid
            ?: prefs.getString("last_uid", "")!!

        if (isGuest) {
            tvGreeting.text = "Hola, Invitado"
            ivAvatar.setImageResource(R.drawable.ic_perfil)
            return
        }

        if (isAdmin) {
            // Carga admin desde RTDB
            FirebaseDatabase.getInstance()
                .getReference("perfiles/admins/$uid")
                .get().addOnSuccessListener { snap ->
                    val nombre = snap.child("nombre").getValue(String::class.java) ?: "Administrador"
                    tvGreeting.text = "Hola, $nombre"
                    val path = snap.child("avatarPath").getValue(String::class.java)
                    if (path != null && File(path).exists()) ivAvatar.setImageBitmap(getCorrectlyOrientedBitmap(path))
                    else ivAvatar.setImageResource(R.drawable.ic_perfil)
                }
            return
        }

        // Cliente: leer de SharedPreferences usando la misma key(uid)
        val nameKey   = "cliente_${uid}_name"
        val avatarKey = "cliente_${uid}_avatar"
        // Mostramos saludo y avatar guardados
        val nombre = prefs.getString(nameKey, "Usuario") ?: "Usuario"
        tvGreeting.text = "Hola, $nombre"
        prefs.getString(avatarKey, null)?.let { path ->
            if (File(path).exists()) ivAvatar.setImageBitmap(getCorrectlyOrientedBitmap(path))
            else ivAvatar.setImageResource(R.drawable.ic_perfil)
        } ?: ivAvatar.setImageResource(R.drawable.ic_perfil)
    }

    private fun getCorrectlyOrientedBitmap(imagePath: String): Bitmap {
        val bitmap = BitmapFactory.decodeFile(imagePath)
        val exif = androidx.exifinterface.media.ExifInterface(imagePath)
        val orient = exif.getAttributeInt(
            androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
            androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
        )
        val matrix = Matrix()
        when (orient) {
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
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
