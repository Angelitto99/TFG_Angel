package com.example.servisurtelecomunicaciones

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class AntenaActivity : AppCompatActivity() {

    // Vistas
    private lateinit var tvTituloAntenas: TextView
    private lateinit var tvSubtituloAntenas: TextView
    private lateinit var tvDescripcionGeneral: TextView
    private lateinit var descriptionParabolic: TextView
    private lateinit var imgParabolic: ImageView
    private lateinit var descriptionTerrestrial: TextView
    private lateinit var imgTerrestrial: ImageView
    private lateinit var btnPedirPresupuesto: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_antena)

        // Título y subtítulo
        tvTituloAntenas = findViewById(R.id.tvTituloAntenas)
        tvSubtituloAntenas = findViewById(R.id.tvSubtituloAntenas)

        // Descripción general
        tvDescripcionGeneral = findViewById(R.id.tvDescripcionGeneral)

        // Parabólica
        descriptionParabolic = findViewById(R.id.descriptionParabolic)
        imgParabolic = findViewById(R.id.imgParabolic)

        // Terrestre
        descriptionTerrestrial = findViewById(R.id.descriptionTerrestrial)
        imgTerrestrial = findViewById(R.id.imgTerrestrial)

        // Botón
        btnPedirPresupuesto = findViewById(R.id.btnPedirPresupuesto)

        // Asignaciones de texto
        tvTituloAntenas.text = "Antenas"
        tvSubtituloAntenas.text = "Conectividad y Señal de TV"
        tvDescripcionGeneral.text = "En ServisurTelecomunicaciones, ofrecemos soluciones integrales para la recepción de señales de televisión. Ya sea con antenas parabólicas de amplia cobertura o antenas terrestres para canales locales, nuestro equipo garantiza una instalación profesional y de alta calidad."

        descriptionParabolic.text = """
            Las antenas parabólicas permiten captar señales de televisión desde satélites en órbita. 
            Se utilizan principalmente para acceder a canales internacionales y de alta definición.
        """.trimIndent()

        descriptionTerrestrial.text = """
            Las antenas terrestres reciben señales de TV desde torres de transmisión locales, 
            perfectas para canales nacionales y contenidos regionales.
        """.trimIndent()

        // Cargar imágenes con Glide (optimizar el tamaño)
        Glide.with(this)
            .load(R.drawable.ic_parabolica)  // Asegúrate de que ic_parabolica sea una imagen optimizada
            .override(600, 400)
            .centerCrop()
            .into(imgParabolic)

        Glide.with(this)
            .load(R.drawable.ic_terrestre)   // Asegúrate de que ic_terrestre sea una imagen optimizada
            .override(600, 400)
            .centerCrop()
            .into(imgTerrestrial)

        // Botón "Pedir Presupuesto"
        btnPedirPresupuesto.setOnClickListener {
            val intent = Intent(this, FormularioAvisoActivity::class.java)
            startActivity(intent)
        }
    }
}
