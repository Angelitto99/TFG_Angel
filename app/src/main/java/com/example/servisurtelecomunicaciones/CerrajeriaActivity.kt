package com.example.servisurtelecomunicaciones

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class CerrajeriaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cerrajeria)

        // Cargar imagen de muelle usando Glide
        val ivMuelle = findViewById<ImageView>(R.id.ivMuelle)
        Glide.with(this)
            .load(R.drawable.ic_muelle) // Reemplaza por tu imagen optimizada
            .override(1200, 1200)         // Ajusta el tamaño (en píxeles) según necesites
            .centerCrop()
            .into(ivMuelle)

        // Cargar imagen de bombillo usando Glide
        val ivBombillo = findViewById<ImageView>(R.id.ivBombillo)
        Glide.with(this)
            .load(R.drawable.ic_bombillo) // Reemplaza por tu imagen optimizada
            .override(1200, 1200)
            .centerCrop()
            .into(ivBombillo)

        // Botón Pedir Presupuesto: abre el formulario
        val btnPedirPresupuesto = findViewById<Button>(R.id.btnPedirPresupuesto)
        btnPedirPresupuesto.setOnClickListener {
            val intent = Intent(this, FormularioAvisoActivity::class.java)
            startActivity(intent)
        }
    }
}
