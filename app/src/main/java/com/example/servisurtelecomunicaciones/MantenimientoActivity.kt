package com.example.servisurtelecomunicaciones

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class MantenimientoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mantenimiento)

        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val isGuest = prefs.getBoolean("is_guest", false)

        // Imagen de mantenimiento de portero
        val imgPortero = findViewById<ImageView>(R.id.imgPortero)
        Glide.with(this)
            .load(R.drawable.ic_arreglando_portero)
            .override(1600, 1600)
            .fitCenter()
            .into(imgPortero)

        // Imagen de mantenimiento de antena
        val imgAntena = findViewById<ImageView>(R.id.imgAntena)
        Glide.with(this)
            .load(R.drawable.ic_arreglando_antena)
            .override(1600, 1600)
            .fitCenter()
            .into(imgAntena)

        // Botón “Pedir información”
        val btnPedirInformacion = findViewById<Button>(R.id.btnPedirInformacion)
        btnPedirInformacion.setOnClickListener {
            if (isGuest) {
                toastConLogo("Debes registrarte para usar esta función")
            } else {
                val intent = Intent(this, FormularioAvisoActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun toastConLogo(msg: String) {
        val layout = layoutInflater.inflate(R.layout.toast_custom_logo, findViewById(android.R.id.content), false)
        layout.findViewById<TextView>(R.id.toastText).text = msg

        Toast(applicationContext).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(android.view.Gravity.CENTER, 0, 250)
            show()
        }
    }
}
