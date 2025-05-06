package com.example.servisurtelecomunicaciones

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class CCTVActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cctv)

        // Referencia a la imagen principal de CCTV
        val ivCCTV = findViewById<ImageView>(R.id.ivCCTV)

        // Cargar la imagen con Glide (ic_cctvpagina). Ajusta el nombre si tu drawable es otro
        Glide.with(this)
            .load(R.drawable.ic_cctvpagina) // Usa tu imagen optimizada
            .override(1200, 800)          // Ajusta tamaño si quieres (ancho x alto)
            .centerCrop()
            .into(ivCCTV)

        // Referencia al botón "Pedir presupuesto"
        val btnPedirPresupuesto = findViewById<Button>(R.id.btnPedirPresupuesto)
        btnPedirPresupuesto.setOnClickListener {
            val intent = Intent(this, FormularioAvisoActivity::class.java)
            startActivity(intent)
        }
    }
}
