package com.example.servisurtelecomunicaciones

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class AboutUsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        // Cargar imágenes en horizontal
        val imgVehiculos = findViewById<ImageView>(R.id.imgVehiculos)
        Glide.with(this)
            .load(R.drawable.ic_vehiculos)
            .override(1500, 1500)
            .fitCenter()
            .into(imgVehiculos)

        val imgTienda = findViewById<ImageView>(R.id.imgTienda)
        Glide.with(this)
            .load(R.drawable.ic_tienda)
            .override(1500, 1500)
            .fitCenter()
            .into(imgTienda)

        // Botón para abrir Google Maps
        val btnUbicacion = findViewById<Button>(R.id.btnUbicacion)
        btnUbicacion.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:0,0?q=Servisur+Telecomunicaciones")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }

        // Botón para abrir la web
        val btnVerWeb = findViewById<Button>(R.id.btnVerWeb)
        btnVerWeb.setOnClickListener {
            val url = "https://www.servisurtelecomunicaciones.com/"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }
}
