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

class AntenaActivity : AppCompatActivity() {

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

        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val isGuest = prefs.getBoolean("is_guest", false)

        tvTituloAntenas = findViewById(R.id.tvTituloAntenas)
        tvSubtituloAntenas = findViewById(R.id.tvSubtituloAntenas)
        tvDescripcionGeneral = findViewById(R.id.tvDescripcionGeneral)
        descriptionParabolic = findViewById(R.id.descriptionParabolic)
        imgParabolic = findViewById(R.id.imgParabolic)
        descriptionTerrestrial = findViewById(R.id.descriptionTerrestrial)
        imgTerrestrial = findViewById(R.id.imgTerrestrial)
        btnPedirPresupuesto = findViewById(R.id.btnPedirPresupuesto)

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

        Glide.with(this)
            .load(R.drawable.ic_parabolica)
            .override(600, 400)
            .centerCrop()
            .into(imgParabolic)

        Glide.with(this)
            .load(R.drawable.ic_terrestre)
            .override(600, 400)
            .centerCrop()
            .into(imgTerrestrial)

        btnPedirPresupuesto.setOnClickListener {
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

