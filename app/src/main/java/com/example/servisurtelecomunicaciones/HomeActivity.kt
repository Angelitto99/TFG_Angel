package com.example.servisurtelecomunicaciones

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView

class HomeActivity : AppCompatActivity() {

    companion object {
        private const val PREFS = "prefs"
        private const val KEY_NAME = "user_name"
        private const val KEY_AVATAR = "user_avatar"
    }

    private lateinit var ivAvatar: CircleImageView
    private lateinit var tvGreeting: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Avatar y saludo
        ivAvatar = findViewById(R.id.ivUserAvatar)
        tvGreeting = findViewById(R.id.tvHolaUsuario)

        ivAvatar.setOnClickListener {
            startActivity(Intent(this, EditarPerfilActivity::class.java))
        }

        // Antenas
        val cardAntenas = findViewById<CardView>(R.id.Antenas)
        val iconAntenas = findViewById<ImageView>(R.id.iconSatelliteTV)
        Glide.with(this).load(R.drawable.ic_satellite).override(48, 48).centerInside().into(iconAntenas)
        cardAntenas.setOnClickListener {
            startActivity(Intent(this, AntenaActivity::class.java))
        }

        // Videoporteros
        val cardVideoporteros = findViewById<CardView>(R.id.cardVideoporteros)
        val iconVideoporteros = findViewById<ImageView>(R.id.iconVideoporteros)
        Glide.with(this).load(R.drawable.ic_videoportero).override(48, 48).centerInside().into(iconVideoporteros)
        cardVideoporteros.setOnClickListener {
            startActivity(Intent(this, VideoporteroActivity::class.java))
        }

        // Electricidad
        val cardElectricidad = findViewById<CardView>(R.id.cardElectricidad)
        val iconElectricidad = findViewById<ImageView>(R.id.iconElectricidad)
        Glide.with(this).load(R.drawable.ic_electricidad).override(48, 48).centerInside().into(iconElectricidad)
        cardElectricidad.setOnClickListener {
            startActivity(Intent(this, ElectricidadActivity::class.java))
        }

        // CCTV
        val cardCCTV = findViewById<CardView>(R.id.cardCCTV)
        val iconCCTV = findViewById<ImageView>(R.id.iconCCTV)
        iconCCTV.setImageResource(R.drawable.ic_cctv)
        cardCCTV.setOnClickListener {
            startActivity(Intent(this, CCTVActivity::class.java))
        }

        // Cerrajería
        val cardCerrajeria = findViewById<CardView>(R.id.cardCerrajeria)
        val iconCerrajeria = findViewById<ImageView>(R.id.iconCerrajeria)
        iconCerrajeria.setImageResource(R.drawable.ic_cerrajeria)
        cardCerrajeria.setOnClickListener {
            startActivity(Intent(this, CerrajeriaActivity::class.java))
        }

        // Mantenimiento
        val cardMantenimiento = findViewById<CardView>(R.id.cardMantenimiento)
        val iconMantenimiento = findViewById<ImageView>(R.id.iconMantenimiento)
        iconMantenimiento.setImageResource(R.drawable.ic_mantenimiento)
        cardMantenimiento.setOnClickListener {
            startActivity(Intent(this, MantenimientoActivity::class.java))
        }

        // Botones inferiores
        findViewById<Button>(R.id.buttonAboutUs).setOnClickListener {
            startActivity(Intent(this, AboutUsActivity::class.java))
        }

        findViewById<Button>(R.id.websiteButton).setOnClickListener {
            val url = "https://www.servisurtelecomunicaciones.com/"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        findViewById<Button>(R.id.buttonIncident).setOnClickListener {
            startActivity(Intent(this, IncidentActivity::class.java))
        }

        findViewById<Button>(R.id.btnCerrarSesion).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).apply {
                setGravity(Gravity.CENTER, 0, 0)
            }.show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val nombre = prefs.getString(KEY_NAME, "Usuario") ?: "Usuario"
        tvGreeting.text = "Hola, $nombre"

        val avatarUri = prefs.getString(KEY_AVATAR, null)
        if (avatarUri != null) {
            Glide.with(this)
                .load(Uri.parse(avatarUri))
                .placeholder(R.drawable.ic_perfil)
                .into(ivAvatar)
        } else {
            ivAvatar.setImageResource(R.drawable.ic_perfil)
        }
    }
}
