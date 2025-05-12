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

        ivAvatar = findViewById(R.id.ivUserAvatar)
        tvGreeting = findViewById(R.id.tvHolaUsuario)

        ivAvatar.setOnClickListener {
            startActivity(Intent(this, EditarPerfilClienteActivity::class.java))
        }

        findViewById<CardView>(R.id.Antenas).apply {
            setOnClickListener { startActivity(Intent(context, AntenaActivity::class.java)) }
        }

        Glide.with(this).load(R.drawable.ic_satellite_new).override(48, 48)
            .centerInside().into(findViewById(R.id.iconSatelliteTV))

        findViewById<CardView>(R.id.cardVideoporteros).apply {
            setOnClickListener { startActivity(Intent(context, VideoporteroActivity::class.java)) }
        }
        Glide.with(this).load(R.drawable.ic_videoportero_new).override(48, 48)
            .centerInside().into(findViewById(R.id.iconVideoporteros))

        findViewById<CardView>(R.id.cardElectricidad).apply {
            setOnClickListener { startActivity(Intent(context, ElectricidadActivity::class.java)) }
        }
        Glide.with(this).load(R.drawable.ic_electricidad_new).override(48, 48)
            .centerInside().into(findViewById(R.id.iconElectricidad))

        findViewById<CardView>(R.id.cardCCTV).apply {
            setOnClickListener { startActivity(Intent(context, CCTVActivity::class.java)) }
        }
        findViewById<ImageView>(R.id.iconCCTV).setImageResource(R.drawable.ic_cctv)

        findViewById<CardView>(R.id.cardCerrajeria).apply {
            setOnClickListener { startActivity(Intent(context, CerrajeriaActivity::class.java)) }
        }
        findViewById<ImageView>(R.id.iconCerrajeria).setImageResource(R.drawable.ic_cerrajeria)

        findViewById<CardView>(R.id.cardMantenimiento).apply {
            setOnClickListener { startActivity(Intent(context, MantenimientoActivity::class.java)) }
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
            startActivity(Intent(this, IncidentActivity::class.java))
        }

        findViewById<Button>(R.id.btnCerrarSesion).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).apply {
                setGravity(Gravity.CENTER, 0, 0)
            }.show()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
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
