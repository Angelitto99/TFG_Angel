package com.example.servisurtelecomunicaciones

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import android.util.Log

class BrandInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brand_info)

        // Recuperar el nombre de la marca pasado por el Intent
        val brandName = intent.getStringExtra("brand_name")

        // Verificar si el nombre de la marca se ha recibido correctamente
        Log.d("BrandInfoActivity", "Marca recibida: $brandName")

        // Mostrar el nombre de la marca en el TextView
        val brandTextView = findViewById<TextView>(R.id.brandTextView)
        brandTextView.text = "Información de la marca: $brandName"

        // Buscar el ImageView donde se mostrará la imagen de la marca
        val brandImageView = findViewById<ImageView>(R.id.brandImageView)

        // Asignar la imagen correcta según el nombre de la marca
        when (brandName) {
            "Tegui" -> brandImageView.setImageResource(R.drawable.ic_tegui)
            "Comelit" -> brandImageView.setImageResource(R.drawable.ic_comelit)
            else -> brandImageView.setImageResource(R.drawable.ic_launcher_foreground) // Por defecto
        }
    }
}

