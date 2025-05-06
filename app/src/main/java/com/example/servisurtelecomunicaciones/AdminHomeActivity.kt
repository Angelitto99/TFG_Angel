package com.example.servisurtelecomunicaciones

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView

class AdminHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        // AVATAR → editar perfil
        findViewById<CircleImageView>(R.id.ivAdminAvatar).also { iv ->
            Glide.with(this)
                .load(R.drawable.ic_perfil)
                .into(iv)
            iv.setOnClickListener {
                startActivity(Intent(this, EditProfileActivity::class.java))
            }
        }

        // Ver incidencias
        findViewById<CardView>(R.id.cardVerIncidencias).setOnClickListener {
            startActivity(Intent(this, IncidenciasListActivity::class.java))
        }

        // Reportar incidencia
        findViewById<CardView>(R.id.cardReportar).setOnClickListener {
            startActivity(Intent(this, IncidentActivity::class.java))
        }

        // Facturas
        findViewById<CardView>(R.id.cardFacturas).setOnClickListener {
            startActivity(Intent(this, FacturasActivity::class.java))
        }

        // Presupuestos
        findViewById<CardView>(R.id.cardPresupuestos).setOnClickListener {
            startActivity(Intent(this, PresupuestosActivity::class.java))
        }

        // Cerrar sesión
        val cerrarSesionBtn = findViewById<Button>(R.id.btnCerrarSesionAdmin)
        cerrarSesionBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val toast = Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
