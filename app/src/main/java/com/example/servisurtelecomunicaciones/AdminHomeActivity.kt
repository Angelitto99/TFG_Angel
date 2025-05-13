package com.example.servisurtelecomunicaciones

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import de.hdodenhof.circleimageview.CircleImageView
import com.google.firebase.auth.FirebaseAuth
import java.io.File

class AdminHomeActivity : AppCompatActivity() {

    companion object {
        private const val PREFS = "prefs"
        private const val KEY_NAME = "admin_name"
        private const val KEY_AVATAR = "admin_avatar"
    }

    private lateinit var ivAvatar: CircleImageView
    private lateinit var tvGreeting: TextView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        // AVATAR + SALUDO
        ivAvatar    = findViewById(R.id.ivAdminAvatar)
        tvGreeting  = findViewById(R.id.tvHolaAdmin)
        btnLogout   = findViewById(R.id.btnCerrarSesionAdmin)

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
            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT)
                .apply { setGravity(Gravity.CENTER, 0, 0) }
                .show()
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
                ivAvatar.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
            } else {
                ivAvatar.setImageResource(R.drawable.ic_perfil)
            }
        } else {
            ivAvatar.setImageResource(R.drawable.ic_perfil)
        }
    }
}
