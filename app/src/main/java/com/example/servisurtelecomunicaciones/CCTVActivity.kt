package com.example.servisurtelecomunicaciones

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class CCTVActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cctv)

        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val isGuest = prefs.getBoolean("is_guest", false)

        val ivCCTV = findViewById<ImageView>(R.id.ivCCTV)
        Glide.with(this)
            .load(R.drawable.ic_cctvpagina)
            .override(1200, 800)
            .centerCrop()
            .into(ivCCTV)

        val btnPedirPresupuesto = findViewById<Button>(R.id.btnPedirPresupuesto)
        btnPedirPresupuesto.setOnClickListener {
            if (isGuest) {
                toastConLogo("Debes registrarte para usar esta función")
            } else {
                startActivity(Intent(this, FormularioAvisoActivity::class.java))
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
